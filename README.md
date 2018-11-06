# Example Play Web Service

This is just a small service using Play 2.6 and a few other tools I was interested in experimenting with
including:
* [Shapeless type tags](http://www.vlachjosef.com/tagged-types-introduction/)
* [Play Action refiners](https://www.playframework.com/documentation/2.6.x/ScalaActionsComposition)
* [Circe Body Parsers for Play](https://github.com/jilen/play-circe)

All endpoints validate the userId's and itemId's ensuring they're of correct
length and only contain valid alphanumeric characters 

* GET       /Example Returns the users current watch list
* PATCH     /Example Receives a AddRequest with a item ID and adds that to the watch list returning the new list
    * In the event a user adds the same item ID to the watch list twice the Example will still only contain 1 reference to the item
    * Other edge cases are documented in the ExampleControllerTest 
* DELETE    /Example Receives a DeleteRequest with a item ID removes said ID from a users watch list and returns the new list
    * In the event the item ID removed is not in the watch list Example will be returned unchanged
    * Other edge cases are documented in the ExampleControllerTest