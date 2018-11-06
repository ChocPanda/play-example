package io.panda.example.controllers

import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, Materializer }
import io.panda.example.controllers.tools._
import io.panda.example.model.client.requests.{ AddRequest, DeleteRequest }
import io.panda.example.model.dao.DataAccessObject
import io.panda.example.model.dto.ExampleDto
import io.panda.example.model.{ ItemId, UserId }
import io.panda.example.test.utils._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{ eq => eqTo, _ }
import org.mockito.Mockito._
import org.scalatest.EitherValues._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatestplus.play.PlaySpec
import play.api.http._
import play.api.i18n.{ Langs, MessagesApi }
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

class ExampleControllerSpec
    extends PlaySpec
    with Results
    with MockitoSugar
    with PropertyChecks
    with ArbitraryInstances {

  implicit private val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit private val mat: Materializer            = ActorMaterializer()(ActorSystem())

  private def contentAsItemsList(r: Future[Result]): List[ItemId] =
    decode[List[ItemId]](contentAsString(r)).right.value

  private val invalidIds = Seq("", "f", "gf0d", "hjfuoldib9j090sfsaj", "fe$", "f&*", "*($")

  private val invalidUserIds = Table("UserId", invalidIds.map(testUserId): _*)
  private val invalidItemIds = Table("ItemId", invalidIds.map(testItemId): _*)

  "GET ExampleController /example" must {

    "return 200 and the listing found by the DAO matching with the matching Id" in forAll { testDto: ExampleDto =>
      val request = FakeRequest(GET, "/example").withHeaders(HeaderNames.BEARER_TOKEN -> testDto.id)

      val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
      when(mockDao.get(testDto.id))
        .thenReturn(Some(testDto))

      val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

      val result = controller.getExample(request)

      status(result) mustEqual OK
      contentAsItemsList(result) must contain theSameElementsAs testDto.items
    }

    "return 200 with an empty watch list is the user has never added anything to the watch list" in {

      val request = FakeRequest(GET, "/example").withHeaders(HeaderNames.BEARER_TOKEN -> validUserId)

      val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
      when(mockDao.get(validUserId)).thenReturn(None)

      val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

      val result = controller.getExample(request)

      status(result) mustEqual OK
      contentAsItemsList(result) mustBe empty
    }

    "return 401 if the request doesn't contain a bearer token" in {
      val request = FakeRequest(GET, "/example")

      val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
      when(mockDao.get(validUserId)).thenReturn(None)

      val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

      val result = controller.getExample(request)

      status(result) mustEqual UNAUTHORIZED
    }

    "return 401 if the request contains an invalid user ID as the bearer token" in forAll(invalidUserIds) {
      invalidUserId =>
        val request = FakeRequest(GET, "/example").withHeaders(HeaderNames.BEARER_TOKEN -> invalidUserId)

        val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
        when(mockDao.get(invalidUserId)).thenReturn(None)

        val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

        val result = controller.getExample(request)

        status(result) mustEqual UNAUTHORIZED
    }
  }

  "PATCH ExampleController /example" must {

    "return 200 with an updated Example if the user has other item in their Example" in forAll { testDto: ExampleDto =>
      val request = FakeRequest(PATCH, "/example")
        .withBody(AddRequest(validItemId).asJson.toString())
        .withHeaders(HeaderNames.BEARER_TOKEN -> testDto.id, HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

      val updatedDto = testDto.copy(items = testDto.items + validItemId)
      val mockDao    = mock[DataAccessObject[UserId, ExampleDto]]
      when(mockDao.upsert(any[ExampleDto])(any(classOf[ExampleDto => ExampleDto])))
        .thenReturn(updatedDto)

      val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

      val result = call(controller.addItem, request)

      status(result) mustEqual OK
      contentAsItemsList(result) must contain theSameElementsAs updatedDto.items
    }

    "return 200 with an unchanged Example if the user adds an existing item ID to their Example" in forAll {
      testDto: ExampleDto =>
        whenever(testDto.items.nonEmpty) {
          val newItem = testDto.items.head
          val req     = AddRequest(newItem)

          val request = FakeRequest(PATCH, "/example")
            .withBody(req.asJson.toString())
            .withHeaders(HeaderNames.BEARER_TOKEN -> testDto.id, HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

          val updatedDto = testDto.copy(items = testDto.items + newItem)
          updatedDto mustEqual testDto

          val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
          when(mockDao.upsert(any[ExampleDto])(any(classOf[ExampleDto => ExampleDto])))
            .thenReturn(updatedDto)

          val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

          val result = call(controller.addItem, request)

          status(result) mustEqual OK
          contentAsItemsList(result) must contain theSameElementsAs updatedDto.items
        }
    }

    "return 200 with a new Example if the user had no other item in their Example" in {
      val request = FakeRequest(PATCH, "/example")
        .withBody(AddRequest(validItemId).asJson.toString())
        .withHeaders(HeaderNames.BEARER_TOKEN -> validUserId, HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

      val mockDao        = mock[DataAccessObject[UserId, ExampleDto]]
      val argumentCaptor = ArgumentCaptor.forClass(classOf[ExampleDto])
      when(mockDao.upsert(argumentCaptor.capture())(any(classOf[ExampleDto => ExampleDto])))
        .thenAnswer(_ => argumentCaptor.getValue)

      val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

      val result = call(controller.addItem, request)

      status(result) mustEqual OK
      contentAsItemsList(result) must contain only validItemId
    }

    "return 400 when a client attempts to add an invalid itemID to the Example" in forAll(invalidItemIds) {
      invalidItemId =>
        val req = AddRequest(invalidItemId)

        val request = FakeRequest(PATCH, "/example")
          .withBody(req.asJson.toString())
          .withHeaders(HeaderNames.BEARER_TOKEN -> validUserId, HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

        val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
        when(mockDao.upsert(any[ExampleDto])(any(classOf[ExampleDto => ExampleDto])))
          .thenReturn(ExampleDto(validUserId, Set(invalidItemId)))

        val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

        val result = call(controller.addItem, request)

        status(result) mustEqual BAD_REQUEST
    }

    "return 401 if the request doesn't contain a bearer token" in {
      val request = FakeRequest(PATCH, "/example")
        .withBody(AddRequest(validItemId).asJson.toString())
        .withHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

      val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
      when(mockDao.upsert(any[ExampleDto])(any(classOf[ExampleDto => ExampleDto])))
        .thenReturn(ExampleDto(validUserId, Set(validItemId)))

      val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

      val result = call(controller.addItem, request)

      status(result) mustEqual UNAUTHORIZED
    }

    "return 401 if the request contains an invalid user ID as the bearer token" in forAll(invalidUserIds) {
      invalidUserId =>
        val request = FakeRequest(PATCH, "/example")
          .withBody(AddRequest(validItemId).asJson.toString())
          .withHeaders(HeaderNames.BEARER_TOKEN -> invalidUserId, HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

        val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
        when(mockDao.upsert(any[ExampleDto])(any(classOf[ExampleDto => ExampleDto])))
          .thenReturn(ExampleDto(validUserId, Set(validItemId)))

        val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

        val result = call(controller.addItem, request)

        status(result) mustEqual UNAUTHORIZED
    }
  }

  "DELETE ExampleController /example" must {

    "return 200 and an updated Example" in forAll { testDto: ExampleDto =>
      whenever(testDto.items.nonEmpty) {
        val deletedItem = testItemId(testDto.items.head)
        val body        = DeleteRequest(deletedItem)
        val request = FakeRequest(DELETE, "/example")
          .withBody(body.asJson.toString())
          .withHeaders(HeaderNames.BEARER_TOKEN -> testDto.id, HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

        val updatedDto = testDto.copy(items = testDto.items - deletedItem)
        val mockDao    = mock[DataAccessObject[UserId, ExampleDto]]
        when(mockDao.update(eqTo(testUserId(testDto.id)))(any(classOf[ExampleDto => ExampleDto])))
          .thenReturn(Option(updatedDto))

        val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

        val result = call(controller.deleteFromExample, request)

        status(result) mustEqual OK
        contentAsItemsList(result) must contain theSameElementsAs updatedDto.items
      }
    }

    "return 200 and an updated Example even if nothing was deleted to the dao" in {
      val request = FakeRequest(DELETE, "/example")
        .withBody(DeleteRequest(validItemId).asJson.toString())
        .withHeaders(HeaderNames.BEARER_TOKEN -> validUserId, HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

      val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
      when(mockDao.update(eqTo(validUserId))(any(classOf[ExampleDto => ExampleDto])))
        .thenReturn(None)

      val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

      val result = call(controller.deleteFromExample, request)

      status(result) mustEqual OK
      contentAsItemsList(result) mustBe empty
    }

    "return 400 when a client attempts to remove an invalid itemID to the Example" in
    forAll { testDto: ExampleDto =>
      forAll(invalidItemIds) { invalidItemId =>
        val body = DeleteRequest(invalidItemId)
        val request = FakeRequest(DELETE, "/example")
          .withBody(body.asJson.toString())
          .withHeaders(HeaderNames.BEARER_TOKEN -> testDto.id, HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

        val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
        when(mockDao.update(eqTo(validUserId))(any(classOf[ExampleDto => ExampleDto])))
          .thenReturn(Option(testDto))

        val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

        val result = call(controller.deleteFromExample, request)

        status(result) mustEqual BAD_REQUEST
      }
    }

    "return 401 if the request doesn't contain a bearer token" in {
      val request = FakeRequest(DELETE, "/example")
        .withBody(DeleteRequest(validItemId).asJson.toString())
        .withHeaders(HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

      val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
      when(mockDao.update(any[UserId])(any(classOf[ExampleDto => ExampleDto])))
        .thenReturn(None)

      val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

      val result = call(controller.deleteFromExample, request)

      status(result) mustEqual UNAUTHORIZED
    }

    "return 401 if the request contains an invalid user ID as the bearer token" in forAll(invalidUserIds) {
      invalidUserId =>
        val request = FakeRequest(DELETE, "/example")
          .withBody(DeleteRequest(validItemId).asJson.toString())
          .withHeaders(HeaderNames.BEARER_TOKEN -> invalidUserId, HeaderNames.CONTENT_TYPE -> ContentTypes.JSON)

        val mockDao = mock[DataAccessObject[UserId, ExampleDto]]
        when(mockDao.update(any[UserId])(any(classOf[ExampleDto => ExampleDto])))
          .thenReturn(None)

        val controller = new ExampleController(FakeExampleControllerComponents(mockDao))

        val result = call(controller.deleteFromExample, request)

        status(result) mustEqual UNAUTHORIZED
    }

  }

  case class FakeExampleControllerComponents(
    override val ExampleDao: DataAccessObject[UserId, ExampleDto] = mock[DataAccessObject[UserId, ExampleDto]],
    override val actionBuilder: ActionBuilder[Request, AnyContent] = DefaultActionBuilder(
      stubBodyParser()
    ),
    override val AuthenticateAction: AuthenticatedAction = new AuthenticatedAction(stubBodyParser()),
    override val parsers: PlayBodyParsers = stubPlayBodyParsers(mat),
    override val messagesApi: MessagesApi = stubMessagesApi(),
    override val langs: Langs = stubLangs(),
    override val fileMimeTypes: FileMimeTypes = new DefaultFileMimeTypes(
      FileMimeTypesConfiguration()
    ),
    override val executionContext: ExecutionContext = ec
  ) extends ExampleControllerComponents

}
