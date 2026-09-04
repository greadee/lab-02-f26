# Completing the participation exercise:

## 1. Ensure the cities collection is a mutable state list, and not just a mutable list.
~~~
val _cities = mutableListOf(...)
~~~
Gets replaced with the following line.
~~~
val _cities = mutableStateListOf<String>()
~~~