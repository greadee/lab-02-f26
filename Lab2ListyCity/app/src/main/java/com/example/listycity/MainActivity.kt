package com.example.listycity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.listycity.ui.theme.ListyCityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val cityRepository = CityRepository()


        setContent {
            ListyCityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CityListScreen(
                        cities = cityRepository.cities,
                        onAddCity = { cityRepository.addCity(it)},
                        onDeleteCity = { cityRepository.delCity(it) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

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

class CityRepository {
    // keep and create a mutable list of city names so that other classes cannot change it directly.
    private val _cities = mutableStateListOf<String>()

    // get a read only list for UI to display
    val cities: List<String>
        get() = _cities

    fun addCity(city: String) { _cities.add(city)}

    fun delCity(city: String) {_cities.remove(city)}
}