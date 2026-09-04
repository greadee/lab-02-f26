# Completing the participation exercise:

## 1. Ensure the cities collection is a mutable state list, and not just a mutable list.
### I.
~~~
val _cities = mutableListOf(...)
~~~
Gets replaced with the following line.
~~~
val _cities = mutableStateListOf<String>()
~~~

## 2. Making the CityList items clickable 
### I.
~~~
LazyColumn(modifier = modifier.fillMaxSize()) {
    items(cities) { city -> 
        CityRow(city = city)
    } 
}
~~~
LazyColumn gets changes to include an OnClickListener
~~~
LazyColumn(modifier = modifier.fillMaxSize()) { 
    items(cities) { city -> 
        CityRow(city = city,
            onClick = {
                // do something when this city is clicked
            }
        )
    }
}
~~~
### II.
~~~
@Composable 
fun CityRow(city: String) { 
    Text( 
        text = city, 
        fontSize = 28.sp, 
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp) ) }
~~~
Adding a click listener to the CityRow function.
~~~
@Composable
fun CityRow(city: String, onClick: () -> Unit
) {
    Text(
        text = city,
        fontSize = 28.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp)
    )
}
~~~