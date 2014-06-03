### Known Issues

#### There are several issues related to bugs in Java 8.

#####The following appear to be resolved at least as of 8u20ea build 10:

1. NullPointerExceptions with Mac gesture support.
2. LINEAR used an enum constant in CSS files gives warnings ([RT-36491](https://javafx-jira.kenai.com/browse/RT-36491)).
3. Changing the axes limits using the mouse may cause the chart to reposition inappropriately in some 
ancestor containers such as a TitledPane which dynamically resizes its contents. To fix that:
(A). place the chart in a Pane
(B). set the Pane prefWidth and prefHeight to a positive value. 

#####The following appear to be resolved at least as of 8u20ea build 14:

#####The following are unresolved:

