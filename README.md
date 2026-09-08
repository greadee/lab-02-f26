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

We can add objects to both a mutableList and a mustableStateList, but a mutableList will not be able to notify of a dataset change, while a mutableStateList will.

## 2. Making the CityList items clickable 
### I.
First, we must add a click listener `onClick` to the `CityRow` function so that each row of our list can be clicked.
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

### II.
We now have to update the `CityRow()` declaration inside `LazyColumn()` to include the new `onClick` parameter.
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

## 3. Implementing the ability to delete CityList items
### To start...
Add a new delete function to the CityRepository class
~~~
fun delCity(city: String) {_cities.remove(city)}
~~~
Update the declaration for CityListScreen to include the onDeleteCity click listener
~~~
@Composable
fun CityListScreen(
    cities: List<String>,
    onAddCity: (String) -> Unit,
    onDeleteCity: (String) -> Unit, // !!!
    modifier: Modifier = Modifier
) {...}
~~~
Reflect the new declaration in the call for CityListScreen from MainActivity
- Note: `it` is the kotlin keyword for the parameter specified in a lambda function
~~~
setContent {
            ListyCityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CityListScreen(
                        cities = cityRepository.cities,
                        onAddCity = { cityRepository.addCity(it)},
                        onDeleteCity = { cityRepository.delCity(it) }, // !!!
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
~~~

### Approach I: Persistent Delete Button
Inside of CityListScreen, underneath `var newCityName by remember { mutableStateOf("") }` add:
~~~
var selectedCity by remember { mutableStateOf<String?>(null) }
~~~
 
 Then, LazyColumn can now use this new selectedCity list for containing the city currently under click.
~~~
LazyColumn(
    modifier = Modifier.weight(1f)
) {
    items(cities) { city ->
        CityRow(
            city = city,
            onClick = {
                selectedCity = city // !!!
            }
        )
    }
}
~~~

~~~
Button(
    onClick = {
        selectedCity?.let {
            onDeleteCity(it) // !!!
            selectedCity = null // !!!
        }
    },
    enabled = selectedCity != null,
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
) {
    Text("Delete City")
}
~~~

### Approach II: OnClick Dialog
Note: this approach uses new imports `AlertDialog` and `TextButton`.


CityListScreen becomes... 
~~~
@Composable
fun CityListScreen(
    cities: List<String>,
    onAddCity: (String) -> Unit,
    onDeleteCity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newCityName by remember { mutableStateOf("") }
    
    ///////////////////////////////
    var cityToDelete by remember { mutableStateOf<String?>(null) }
    ///////////////////////////////
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {

        Row(
            modifier = Modifier.padding(16.dp)
        ) {

            OutlinedTextField(
                value = newCityName,
                onValueChange = { newCityName = it },
                label = { Text("City Name") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (newCityName.isNotBlank()) {
                        onAddCity(newCityName)
                        newCityName = ""
                    }
                }
            ) {
                Text("Add City")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(cities) { city ->

                CityRow(
                    city = city,
                    ////////////////////////////////////
                    onClick = {
                        cityToDelete = city // click functionality added was delete
                    }
                    ////////////////////////////////////
                )
            }
        }
    }

    // DIALOG
    ////////////////////////////////////
    if (cityToDelete != null) {

        AlertDialog(
            onDismissRequest = {
                cityToDelete = null
            },

            title = {
                Text("Delete City")
            },

            text = {
                Text("Are you sure you want to delete $cityToDelete?")
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        cityToDelete?.let {
                            onDeleteCity(it)
                        }

                        cityToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        cityToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    /////////////////////////////////
}
~~~