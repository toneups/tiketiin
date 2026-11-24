
package com.example.tiketiin.ui.hotel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tiketiin.data.model.Hotel
import com.example.tiketiin.data.remote.dto.CreateHotelRequestDto
import com.example.tiketiin.data.remote.dto.UpdateHotelRequestDto
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HotelManagementScreen(
    viewModel: HotelManagementViewModel
) {
    val state by viewModel.uiState.collectAsState()

    var showForm by remember { mutableStateOf(false) }
    var editingHotel by remember { mutableStateOf<Hotel?>(null) }
    var deletingHotel by remember { mutableStateOf<Hotel?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("4.5") }
    var rooms by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var bookingUrl by remember { mutableStateOf("") }

    fun resetForm() {
        name = ""
        address = ""
        city = ""
        latitude = ""
        longitude = ""
        price = ""
        rating = "4.5"
        rooms = ""
        imageUrl = ""
        bookingUrl = ""
        editingHotel = null
        formError = null
        showForm = false
    }

    fun editHotel(hotel: Hotel) {
        editingHotel = hotel
        name = hotel.name
        address = hotel.address
        city = hotel.city
        latitude = hotel.latitude.toString()
        longitude = hotel.longitude.toString()
        price = hotel.pricePerNight.toString()
        rating = hotel.rating.toString()
        rooms = hotel.availableRooms.toString()
        imageUrl = hotel.imageUrl.orEmpty()
        bookingUrl = hotel.bookingUrl.orEmpty()
        formError = null
        showForm = true
    }

    fun saveHotel() {
        formError = null

        val lat = latitude.toDoubleOrNull()
        val lng = longitude.toDoubleOrNull()
        val hotelPrice = price.toLongOrNull()
        val hotelRating = rating.toDoubleOrNull()
        val availableRooms = rooms.toIntOrNull()

        when {
            name.isBlank() ->
                formError = "Nama hotel wajib diisi."

            address.isBlank() ->
                formError = "Alamat wajib diisi."

            city.isBlank() ->
                formError = "Kota wajib diisi."

            lat == null ->
                formError = "Latitude harus berupa angka."

            lng == null ->
                formError = "Longitude harus berupa angka."

            lat !in -90.0..90.0 ->
                formError = "Latitude harus antara -90 sampai 90."

            lng !in -180.0..180.0 ->
                formError = "Longitude harus antara -180 sampai 180."

            hotelPrice == null || hotelPrice < 0 ->
                formError = "Harga hotel tidak valid."

            hotelRating == null || hotelRating !in 0.0..5.0 ->
                formError = "Rating harus antara 0 sampai 5."

            availableRooms == null || availableRooms < 0 ->
                formError = "Jumlah kamar tidak valid."
        }

        if (formError != null) return

        val existing = editingHotel

        if (existing == null) {
            viewModel.createHotel(
                CreateHotelRequestDto(
                    hotelName = name.trim(),
                    address = address.trim(),
                    city = city.trim(),
                    latitude = lat!!,
                    longitude = lng!!,
                    pricePerNight = hotelPrice!!,
                    rating = hotelRating!!,
                    availableRooms = availableRooms!!,
                    imageUrl = imageUrl.trim().ifBlank { null },
                    bookingUrl = bookingUrl.trim().ifBlank { null }
                )
            )
        } else {
            viewModel.updateHotel(
                hotelId = existing.id,
                request = UpdateHotelRequestDto(
                    hotelName = name.trim(),
                    address = address.trim(),
                    city = city.trim(),
                    latitude = lat!!,
                    longitude = lng!!,
                    pricePerNight = hotelPrice!!,
                    rating = hotelRating!!,
                    availableRooms = availableRooms!!,
                    imageUrl = imageUrl.trim().ifBlank { null },
                    bookingUrl = bookingUrl.trim().ifBlank { null }
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadHotels()
    }

    // Tutup form atau dialog hanya setelah operasi berhasil.
    LaunchedEffect(state.message) {
        if (state.message != null) {
            resetForm()
            deletingHotel = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kelola Hotel",
                style = MaterialTheme.typography.headlineSmall
            )

            Button(
                onClick = {
                    resetForm()
                    showForm = true
                },
                enabled = !state.isSaving
            ) {
                Text("Tambah")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        state.message?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (editingHotel == null)
                            "Tambah Hotel" else "Edit Hotel",
                        style = MaterialTheme.typography.titleLarge
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Hotel") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Kota") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Harga per Malam (Rp)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    OutlinedTextField(
                        value = rating,
                        onValueChange = { rating = it },
                        label = { Text("Rating (0-5)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    OutlinedTextField(
                        value = rooms,
                        onValueChange = { rooms = it },
                        label = { Text("Kamar Tersedia") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("URL Gambar (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    OutlinedTextField(
                        value = bookingUrl,
                        onValueChange = { bookingUrl = it },
                        label = { Text("URL Booking (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSaving
                    )

                    if (formError != null) {
                        Text(
                            text = formError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { saveHotel() },
                            enabled = !state.isSaving
                        ) {
                            Text(
                                if (state.isSaving)
                                    "Menyimpan..."
                                else "Simpan"
                            )
                        }

                        OutlinedButton(
                            onClick = { resetForm() },
                            enabled = !state.isSaving
                        ) {
                            Text("Batal")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.hotels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada data hotel.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.hotels,
                    key = { it.id }
                ) { hotel ->
                    HotelManagementItem(
                        hotel = hotel,
                        onEdit = { editHotel(hotel) },
                        onDelete = { deletingHotel = hotel }
                    )
                }
            }
        }
    }

    deletingHotel?.let { hotel ->
        AlertDialog(
            onDismissRequest = {
                if (!state.isSaving) {
                    deletingHotel = null
                }
            },
            title = {
                Text("Hapus Hotel")
            },
            text = {
                Text("Yakin ingin menghapus ${hotel.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHotel(hotel.id)
                    },
                    enabled = !state.isSaving
                ) {
                    Text(
                        if (state.isSaving) "Menghapus..."
                        else "Hapus"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deletingHotel = null },
                    enabled = !state.isSaving
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun HotelManagementItem(
    hotel: Hotel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val currency = remember(hotel.pricePerNight) {
        NumberFormat.getCurrencyInstance(
            Locale("id", "ID")
        ).format(hotel.pricePerNight)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = hotel.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(hotel.area)
            Text("Harga: $currency")
            Text("Rating: ${hotel.rating} / 5")
            Text("Kamar tersedia: ${hotel.availableRooms}")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onEdit) {
                    Text("Edit")
                }

                OutlinedButton(onClick = onDelete) {
                    Text("Hapus")
                }
            }
        }
    }
}
