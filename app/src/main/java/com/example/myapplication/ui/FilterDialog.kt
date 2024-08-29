package com.example.myapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun CustomDialog(showDialog: Boolean, onDismiss: () -> Unit, onSelectFilter: (String) -> Unit) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(25.dp),
                color = Color(0xFF333333),
                contentColor = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Sort By",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Artist (A - Z)",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            onSelectFilter("AlphabeticalArtist")
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Album (A - Z)",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            onSelectFilter("AlphabeticalAlbum")
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Newest Added",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            onSelectFilter("NewestTimestamp")
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Oldest Added",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            onSelectFilter("OldestTimestamp")
                            onDismiss()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
