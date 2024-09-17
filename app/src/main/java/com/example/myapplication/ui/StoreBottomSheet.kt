package com.example.myapplication.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.myapplication.R
import com.google.android.libraries.places.api.model.Place
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.myapplication.utils.StarRating
import com.example.myapplication.utils.isStoreOpen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreBottomSheet(
    store: Place,
    storeImages: List<String>,
    isLoading: Boolean,
    sheetState: SheetState,
    onClose: () -> Unit,
    onDirectionsClick: (String) -> Unit,
    onWebsiteClick: (String) -> Unit
) {

    var selectedImage by remember { mutableStateOf<String?>(null) }
    var showImageDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = colorResource(id = R.color.background),
        contentColor = colorResource(id = R.color.primary_text)
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .background(colorResource(R.color.background))
        ) {
            // Store name and close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                store.name?.let {
                    Text(
                        text = it,
                        style = TextStyle(fontSize = 26.sp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.bubble),
                        contentColor = colorResource(id = R.color.primary_text)
                    ),
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(50.dp),
                    shape = RoundedCornerShape(50.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(25.dp),
                        tint = colorResource(id = R.color.primary_text)
                    )
                }
            }

            // Store rating, reviews, and open status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(text = "${store.rating}", color = colorResource(id = R.color.secondary_text))
                StarRating(rating = store.rating?.toFloat() ?: 0f)
                Text(text = "(${store.reviews?.size})", color = colorResource(id = R.color.secondary_text))
            }

            val isOpen = isStoreOpen(place = store)
            Text(text = isOpen, modifier = Modifier.padding(bottom = 5.dp))

            // Buttons for Directions and Website
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Button(
                    onClick = { onDirectionsClick(store.address ?: "") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.bubble),
                        contentColor = colorResource(id = R.color.primary_text)
                    ),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(text = "Directions", style = TextStyle(fontSize = 17.sp))
                }

                store.websiteUri?.let {
                    Button(
                        onClick = { onWebsiteClick(it.toString()) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.bubble),
                            contentColor = colorResource(id = R.color.primary_text)
                        ),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(text = "Website", style = TextStyle(fontSize = 17.sp))
                    }
                }
            }

            // Store images
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .background(
                                colorResource(id = R.color.background),
                                RoundedCornerShape(8.dp)
                            ),
                    )
                } else if (storeImages.isNotEmpty()) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(150.dp),
                        verticalItemSpacing = 4.dp,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        content = {
                            items(storeImages) { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            selectedImage = imageUrl
                                            showImageDialog = true
                                        }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
