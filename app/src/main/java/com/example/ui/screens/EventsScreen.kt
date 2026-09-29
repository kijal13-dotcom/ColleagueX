package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.theme.colleagueXTextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EventEntity
import com.example.ui.components.UserAvatar
import com.example.ui.theme.WorkCircleBackground
import com.example.ui.theme.WorkCircleBlue
import com.example.ui.theme.WorkCircleCardBorder
import com.example.ui.theme.WorkCircleNavy
import com.example.ui.theme.WorkCircleSecondaryText
import com.example.ui.theme.WorkCircleVerifiedGreen

@Composable
fun EventsScreen(
    events: List<EventEntity>,
    onToggleRsvp: (Long, Boolean) -> Unit,
    onToggleSave: (Long, Boolean) -> Unit,
    onHostEvent: (title: String, type: String, city: String, scope: String, venue: String, date: String, time: String, desc: String, cat: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All Events") }
    var showHostDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All Events", "Mixers & Socials", "Tech Meetups", "Virtual Webinars", "Roundtables")

    val filteredEvents = remember(events, selectedFilter) {
        events.filter { event ->
            when (selectedFilter) {
                "Mixers & Socials" -> event.eventType == "MIXER" || event.category.contains("Social", ignoreCase = true)
                "Tech Meetups" -> event.eventType == "MEETUP"
                "Virtual Webinars" -> event.eventType == "WEBINAR"
                "Roundtables" -> event.eventType == "ROUNDTABLE"
                else -> true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkCircleBackground)
            .testTag("colleaguex_events_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Header Banner
            item {
                Surface(
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ColleagueX Events",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = WorkCircleNavy
                                )
                                Text(
                                    text = "Professional & Social Gatherings",
                                    fontSize = 11.sp,
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Meet colleagues face-to-face in your city or join nationwide live streams on system architecture, startup leadership, and career growth.",
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter Pills
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filterOptions) { filter ->
                                val isSelected = selectedFilter == filter
                                Surface(
                                    color = if (isSelected) Color(0xFFDC2626) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedFilter = filter }
                                ) {
                                    Text(
                                        text = filter,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else WorkCircleNavy,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Events Count Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Colleague Gatherings (${filteredEvents.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkCircleNavy
                    )
                    Text(
                        text = "In-Person & Virtual",
                        fontSize = 11.sp,
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 3. Events Cards Stream
            items(filteredEvents) { event ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("event_card_${event.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = when (event.eventType) {
                                    "MIXER" -> Color(0xFFFEF3C7)
                                    "WEBINAR" -> Color(0xFFEDE9FE)
                                    "ROUNDTABLE" -> Color(0xFFEFF6FF)
                                    else -> Color(0xFFECFDF5)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = event.eventType,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (event.eventType) {
                                        "MIXER" -> Color(0xFFD97706)
                                        "WEBINAR" -> Color(0xFF7C3AED)
                                        "ROUNDTABLE" -> WorkCircleBlue
                                        else -> WorkCircleVerifiedGreen
                                    },
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = { onToggleSave(event.id, event.isSaved) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (event.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save Event",
                                    tint = if (event.isSaved) Color(0xFFDC2626) else WorkCircleSecondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = event.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkCircleNavy
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Date & Time Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.dateText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WorkCircleNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = WorkCircleSecondaryText, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.timeText,
                                fontSize = 11.sp,
                                color = WorkCircleSecondaryText
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Venue Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkCircleBlue, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.venue,
                                fontSize = 11.sp,
                                color = WorkCircleBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = event.description,
                            fontSize = 12.sp,
                            color = WorkCircleSecondaryText,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Organizer & Attendee Count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(initials = event.organizerAvatar, sizeDp = 28)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = event.organizerName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WorkCircleNavy
                                    )
                                    Text(
                                        text = event.organizerRole,
                                        fontSize = 9.sp,
                                        color = WorkCircleSecondaryText
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.People, contentDescription = null, tint = WorkCircleSecondaryText, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${event.attendeesCount} Colleagues Going",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkCircleNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // RSVP Action Button
                        Button(
                            onClick = { onToggleRsvp(event.id, event.isRsvp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (event.isRsvp) WorkCircleVerifiedGreen else Color(0xFFDC2626)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (event.isRsvp) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Registered • You're Going", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("RSVP to Attend", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Host an Event FAB
        FloatingActionButton(
            onClick = { showHostDialog = true },
            containerColor = Color(0xFFDC2626),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("host_event_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Host an Event")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Host an Event", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Host Event Dialog
    if (showHostDialog) {
        var eventTitle by remember { mutableStateOf("") }
        var eventCity by remember { mutableStateOf("Bengaluru") }
        var eventVenue by remember { mutableStateOf("") }
        var eventDate by remember { mutableStateOf("Saturday, Nov 16") }
        var eventTime by remember { mutableStateOf("5:00 PM IST") }
        var eventDesc by remember { mutableStateOf("") }
        var eventType by remember { mutableStateOf("MEETUP") }

        AlertDialog(
            onDismissRequest = { showHostDialog = false },
            title = { Text("Host a ColleagueX Event", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column {
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                        label = { Text("Event Title", fontSize = 11.sp) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = eventCity,
                            onValueChange = { eventCity = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                            label = { Text("City", fontSize = 11.sp) },
                            colors = colleagueXTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = eventType,
                            onValueChange = { eventType = it },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                            label = { Text("Type (MEETUP/WEBINAR)", fontSize = 11.sp) },
                            colors = colleagueXTextFieldColors(),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = eventVenue,
                        onValueChange = { eventVenue = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                        label = { Text("Venue / Online URL", fontSize = 11.sp) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = eventDesc,
                        onValueChange = { eventDesc = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 13.sp),
                        label = { Text("Event Description & Agenda", fontSize = 11.sp) },
                        colors = colleagueXTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (eventTitle.isNotBlank()) {
                            onHostEvent(
                                eventTitle, eventType, eventCity, "LOCAL",
                                eventVenue.ifBlank { "Cafe Coffee Day / WeWork" },
                                eventDate, eventTime,
                                eventDesc.ifBlank { "Exciting meetup with fellow colleagues." },
                                "Networking"
                            )
                            showHostDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Publish Event", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
