Lab 2 Walkthrough: ListyCity Participation Exercise

This walkthrough explains not only what code to add, but why the solution is structured this way in Kotlin and Jetpack Compose.

The goal of the participation exercise is to extend the ListyCity app so that:

- the city list uses Compose-aware state
- each city row can respond to clicks
- the user can delete a selected city

These changes connect three major ideas from the lab:

- **Kotlin OOP:** keep app data protected inside a class
- **Jetpack Compose state:** make UI update when data changes
- **Compose event flow:** pass data down into composables and send user actions back up through callbacks

## 1. Use A Compose-Aware City List

In the original version, the city collection may look like this:

```kotlin
val _cities = mutableListOf(...)
```

Replace it with a Compose state list:

```kotlin
private val _cities = mutableStateListOf<String>()
val cities: List<String> = _cities
```

Or, if the lab starter code already includes initial cities:

```kotlin
private val _cities = mutableStateListOf("Edmonton", "Vancouver", "Toronto")
val cities: List<String> = _cities
```

## Why This Matters

Both `mutableListOf` and `mutableStateListOf` can store city names. The important difference is that `mutableStateListOf` is observable by Jetpack Compose.

Compose redraws affected UI when state changes. A regular `MutableList` can change internally, but Compose may not know that the list changed. That means the data could update while the screen still looks stale.

`mutableStateListOf` solves that problem because it is a Compose snapshot state collection. When items are added or removed, Compose can notice the change and recompose the UI that reads the list.

This connects directly to the Android Basics idea:

> When state changes, Compose updates the affected UI.

## Kotlin And OOP Connection

The repository should usually keep the mutable list private:

```kotlin
private val _cities = mutableStateListOf<String>()
val cities: List<String> = _cities
```

This is encapsulation. Other parts of the app can read `cities`, but they cannot directly mutate `_cities`.

Instead of allowing any composable to change the list however it wants, the repository exposes specific functions:

```kotlin
fun addCity(city: String) {
    _cities.add(city)
}

fun deleteCity(city: String) {
    _cities.remove(city)
}
```

This keeps data changes controlled and easier to debug. The UI asks for a change; the repository performs the change.

## Common Pitfalls

- Using `mutableListOf` instead of `mutableStateListOf`, which can cause the UI not to refresh.
- Forgetting to import `mutableStateListOf`.
- Making `_cities` public and mutable, which breaks encapsulation.
- Returning a mutable list directly when the UI only needs read-only access.
- Misspelling `mutableStateListOf`.

## 2. Make City Rows Clickable

The original `CityRow` might only display text:

```kotlin
@Composable
fun CityRow(city: String) {
    Text(
        text = city,
        fontSize = 28.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    )
}
```

To let each row respond to clicks, add an `onClick` callback parameter:

```kotlin
@Composable
fun CityRow(
    city: String,
    onClick: () -> Unit
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
```

## Why This Matters

`CityRow` should not decide what clicking means. It should only report that it was clicked.

That is why the function receives:

```kotlin
onClick: () -> Unit
```

This means "`CityRow` accepts a function that takes no arguments and returns nothing." The parent composable decides what action should happen.

In this lab, clicking a row may mean "select this city" or "ask whether to delete this city." In another app, clicking a row might mean "open a details screen." Keeping the click behavior as a callback makes `CityRow` reusable.

This connects to the Compose idea:

> State flows down; events flow up.

The city name flows down into `CityRow`. The click event flows back up through `onClick`.

## Modifier Order

This code puts `clickable` before `padding`:

```kotlin
modifier = Modifier
    .fillMaxWidth()
    .clickable { onClick() }
    .padding(horizontal = 18.dp, vertical = 14.dp)
```

Modifier order matters in Compose. In this version, the wider row area becomes clickable, then padding affects the text inside it.

If `padding` comes before `clickable`, only the padded content area may feel clickable, which can make the row less comfortable to use.

## Common Pitfalls

- Adding `onClick` to `CityRow` but forgetting to update every place where `CityRow` is called.
- Writing `onClick = onClick()` instead of `onClick = { onClick() }`, which calls the function immediately instead of waiting for a click.
- Forgetting the `.clickable` import.
- Putting click behavior inside the repository. The repository should manage data, not UI interactions.

## 3. Update LazyColumn To Pass Click Events

Before adding click behavior, the list may look like this:

```kotlin
LazyColumn(modifier = modifier.fillMaxSize()) {
    items(cities) { city ->
        CityRow(city = city)
    }
}
```

After updating `CityRow`, each row needs an `onClick` argument:

```kotlin
LazyColumn(modifier = modifier.fillMaxSize()) {
    items(cities) { city ->
        CityRow(
            city = city,
            onClick = {
                // Do something when this city is clicked.
            }
        )
    }
}
```

## Why This Matters

`LazyColumn` creates one row for each item in the list. The `city` variable inside `items(cities) { city -> ... }` represents the current city for that row.

That means each row can have click behavior that refers to its own city:

```kotlin
onClick = {
    selectedCity = city
}
```

Compose uses `LazyColumn` because it is efficient for scrolling lists. It composes visible rows as needed instead of building every row all at once.

## Common Pitfalls

- Forgetting that `city` inside the lambda is the current list item.
- Accidentally using `newCityName` instead of `city` when selecting or deleting.
- Calling `CityRow(city)` without naming arguments after changing the function signature.
- Using a non-lazy `Column` for a potentially long list.

## 4. Add Delete Support To The Repository

Add a delete function to `CityRepository`:

```kotlin
fun deleteCity(city: String) {
    _cities.remove(city)
}
```

Your original draft used:

```kotlin
fun delCity(city: String) {
    _cities.remove(city)
}
```

That works, but `deleteCity` is a clearer Kotlin-style name.

## Why This Matters

Deleting a city changes app data, so it belongs in the repository. The UI should not directly modify `_cities`.

This keeps the app organized:

- `CityRepository` owns the data
- `CityListScreen` displays the data
- `CityRow` displays one item
- callback functions connect user actions back to the repository

## Common Pitfalls

- Trying to call `_cities.remove(city)` directly from `CityListScreen`.
- Forgetting that `_cities` is private and should stay private.
- Removing from a normal mutable list and wondering why the UI does not update.
- Deleting by index when the UI is passing a city `String`.

## 5. Pass Delete Behavior Into CityListScreen

Update `CityListScreen` so it receives an `onDeleteCity` callback:

```kotlin
@Composable
fun CityListScreen(
    cities: List<String>,
    onAddCity: (String) -> Unit,
    onDeleteCity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Screen content
}
```

Then update the call from `MainActivity`:

```kotlin
setContent {
    ListyCityTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CityListScreen(
                cities = cityRepository.cities,
                onAddCity = { cityRepository.addCity(it) },
                onDeleteCity = { cityRepository.deleteCity(it) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
```

## Why This Matters

`onDeleteCity: (String) -> Unit` means "`CityListScreen` receives a function that takes a `String` city name and performs an action."

The screen does not need to know exactly how deletion works. It only needs to call:

```kotlin
onDeleteCity(city)
```

The parent, `MainActivity`, connects that callback to the repository:

```kotlin
onDeleteCity = { cityRepository.deleteCity(it) }
```

Here, `it` is Kotlin shorthand for the single lambda parameter. In this case, `it` is the city name being deleted.

You could also write it more explicitly:

```kotlin
onDeleteCity = { city ->
    cityRepository.deleteCity(city)
}
```

This version is often easier for beginners to read.

## Common Pitfalls

- Forgetting to pass `onDeleteCity` from `MainActivity`.
- Confusing `it` with a special Compose keyword. It is just Kotlin shorthand for a lambda parameter.
- Calling `cityRepository.deleteCity(it)` in the wrong scope, where `it` does not refer to a city.
- Forgetting the comma after `onAddCity = ...` when adding another argument.

## 6. Approach I: Persistent Delete Button

In this approach, clicking a city selects it. A separate button deletes the currently selected city.

Inside `CityListScreen`, add state for the selected city:

```kotlin
var selectedCity by remember { mutableStateOf<String?>(null) }
```

Then update the list:

```kotlin
LazyColumn(
    modifier = Modifier.weight(1f)
) {
    items(cities) { city ->
        CityRow(
            city = city,
            onClick = {
                selectedCity = city
            }
        )
    }
}
```

Then add a delete button:

```kotlin
Button(
    onClick = {
        selectedCity?.let {
            onDeleteCity(it)
            selectedCity = null
        }
    },
    enabled = selectedCity != null,
    modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
) {
    Text("Delete City")
}
```

## Why This Matters

This introduces nullable state:

```kotlin
String?
```

`String?` means the value can either be a city name or `null`.

At the start, no city is selected:

```kotlin
null
```

After a row is clicked, `selectedCity` stores that row's city name.

The delete button is disabled until a city is selected:

```kotlin
enabled = selectedCity != null
```

This is a good UI pattern because it prevents invalid actions. The user cannot delete "nothing."

## Why Use `let`?

This code:

```kotlin
selectedCity?.let {
    onDeleteCity(it)
    selectedCity = null
}
```

means: "If `selectedCity` is not null, run this block and call the non-null value `it`."

It avoids a null crash and gives Kotlin enough information to safely pass the city into `onDeleteCity`.

## Common Pitfalls

- Forgetting the `?` in `String?`, even though the initial value is `null`.
- Forgetting to clear `selectedCity` after deletion.
- Using `Modifier.fillMaxSize()` on `LazyColumn` inside a `Column`, which can leave no room for the delete button. `Modifier.weight(1f)` is usually better here.
- Not giving the user any visual clue about which city is selected.

## Optional Improvement

You can visually highlight the selected city by passing another parameter into `CityRow`, such as `isSelected`, and changing the background color when it is selected. That is not required for the lab, but it makes the UI clearer.

## 7. Approach II: Delete Confirmation Dialog

In this approach, clicking a city opens a confirmation dialog before deleting it.

This version needs these imports:

```kotlin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
```

Inside `CityListScreen`, add state for the city that may be deleted:

```kotlin
var cityToDelete by remember { mutableStateOf<String?>(null) }
```

Then update each row click:

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize()
) {
    items(cities) { city ->
        CityRow(
            city = city,
            onClick = {
                cityToDelete = city
            }
        )
    }
}
```

Finally, show the dialog only when a city has been selected:

```kotlin
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
```

## Why This Matters

The dialog is controlled by state.

When:

```kotlin
cityToDelete == null
```

there is no dialog.

When:

```kotlin
cityToDelete != null
```

Compose includes the `AlertDialog` in the UI.

This is one of the main Compose ideas: the UI is a function of state. Instead of manually opening and closing a dialog through a separate UI manager, you change state and Compose displays the correct UI.

## Why This Is Safer Than Immediate Delete

Deleting on row click alone can surprise users. A confirmation dialog makes the destructive action explicit:

- click a city
- review the confirmation message
- choose Delete or Cancel

This is especially useful for beginner labs because it makes event handling easier to observe.

## Common Pitfalls

- Forgetting to set `cityToDelete = null` after Delete or Cancel.
- Putting the `AlertDialog` inside the `LazyColumn`, which can create confusing behavior.
- Forgetting the `AlertDialog` or `TextButton` imports.
- Calling `onDeleteCity(cityToDelete)` directly even though `cityToDelete` is nullable.
- Displaying the dialog but not actually deleting the city in the confirm button.

## Recommended Final Version

This is a complete version of the main pieces using the dialog approach.

```kotlin
class CityRepository {
    private val _cities = mutableStateListOf("Edmonton", "Vancouver", "Toronto")
    val cities: List<String> = _cities

    fun addCity(city: String) {
        _cities.add(city)
    }

    fun deleteCity(city: String) {
        _cities.remove(city)
    }
}
```

```kotlin
@Composable
fun CityListScreen(
    cities: List<String>,
    onAddCity: (String) -> Unit,
    onDeleteCity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newCityName by remember { mutableStateOf("") }
    var cityToDelete by remember { mutableStateOf<String?>(null) }

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
                    onClick = {
                        cityToDelete = city
                    }
                )
            }
        }
    }

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
}
```

```kotlin
@Composable
fun CityRow(
    city: String,
    onClick: () -> Unit
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
```

## Big Picture Summary

The lab is not only about adding and deleting strings from a list. It is practicing the basic architecture of a Compose app.

`MainActivity` starts the Android screen and calls `setContent`.

`CityRepository` stores and protects app data.

`CityListScreen` receives data and callback functions.

`CityRow` displays one reusable row.

`mutableStateListOf` makes list changes visible to Compose.

`remember { mutableStateOf(...) }` stores temporary UI state, such as text input or the city currently selected for deletion.

Callbacks such as `onAddCity` and `onDeleteCity` let user events travel back up to the owner of the data.

That is the main Compose pattern in this lab:

```text
state down, events up
```

## Debugging Checklist

If the app does not compile, check:

- Did you import `mutableStateListOf`, `mutableStateOf`, `remember`, `LazyColumn`, `items`, `clickable`, `AlertDialog`, and `TextButton`?
- Did you update every `CityListScreen` call after adding `onDeleteCity`?
- Did you update every `CityRow` call after adding `onClick`?
- Are all commas present between function arguments?
- Is `cityToDelete` declared as `String?` if it starts as `null`?

If the app compiles but the UI does not update, check:

- Are you using `mutableStateListOf` instead of `mutableListOf`?
- Are you removing the city through the repository function?
- Is the composable reading `cityRepository.cities`?

If clicking does nothing, check:

- Is `.clickable { onClick() }` attached to the row?
- Did you pass a real lambda into `CityRow`?
- Are you setting `selectedCity` or `cityToDelete` inside the row click?

If the dialog will not close, check:

- Does Cancel set `cityToDelete = null`?
- Does Delete also set `cityToDelete = null` after deleting?
- Does `onDismissRequest` set `cityToDelete = null`?

## Terms To Know

**Composable:** A Kotlin function annotated with `@Composable` that describes part of the UI.

**State:** Data that can change over time and affect what appears on screen.

**Recomposition:** Compose re-running composable functions when state changes.

**Callback:** A function passed into another function so the child can report an event to the parent.

**Lambda:** A function expression, often written inside `{ }`.

**Encapsulation:** An OOP principle where data is protected inside a class and changed through controlled functions.

**Nullable type:** A Kotlin type with `?`, such as `String?`, meaning the value can be either a `String` or `null`.
