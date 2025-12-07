// COMPLETE FIXED VERSION - Replace your CommunityScreen.kt with this

// FILE: app/src/main/java/com/example/trailblazer/ui/screens/CommunityScreen.kt
package com.example.trailblazer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailblazer.net.ApiClient
import com.example.trailblazer.net.PostCreateRequest
import com.example.trailblazer.net.PostDto
import kotlinx.coroutines.launch

data class Post(
    val id: Int,
    val username: String,
    val challengeName: String?,
    val timeAgo: String,
    val content: String,
    val likes: Int,
    val comments: Int,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false
)

@Composable
fun CommunityScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Recent", "Popular", "Friends")

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showNewPostDialog by remember { mutableStateOf(false) }
    var newPostText by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun mapDtoToPost(dto: PostDto): Post {
        return Post(
            id = dto.id,
            username = "Hiker #${dto.userId}",
            challengeName = null,
            timeAgo = dto.createdAt,
            content = dto.body,
            likes = 0,
            comments = 0
        )
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val apiPosts = ApiClient.service.getPosts(limit = 50)
            posts = apiPosts.map(::mapDtoToPost)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load posts."
        } finally {
            isLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // Top Bar
            Surface(
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Community",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Button(
                        onClick = { showNewPostDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, "New Post", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New Post", fontSize = 14.sp)
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF4CAF50)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else if (posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Forum,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFBDBDBD)
                        )
                        Text(
                            text = "No posts yet",
                            fontSize = 18.sp,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = "Be the first to share your adventure!",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(posts) { post ->
                        PostCard(post = post)
                    }
                }
            }

            // Bottom Navigation
            BottomNavigationBar(
                currentScreen = "Community",
                onNavigate = onNavigate
            )
        }

        // New Post Dialog
        if (showNewPostDialog) {
            AlertDialog(
                onDismissRequest = { showNewPostDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newPostText.isNotBlank()) {
                                scope.launch {
                                    try {
                                        ApiClient.service.createPost(
                                            PostCreateRequest(
                                                body = newPostText,
                                                trailId = null
                                            )
                                        )
                                        showNewPostDialog = false
                                        newPostText = ""
                                        // Refresh posts
                                        val apiPosts = ApiClient.service.getPosts(limit = 50)
                                        posts = apiPosts.map(::mapDtoToPost)
                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Post", color = Color(0xFF4CAF50))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showNewPostDialog = false
                        newPostText = ""
                    }) {
                        Text("Cancel", color = Color(0xFF757575))
                    }
                },
                title = {
                    Text("New Post", fontWeight = FontWeight.Bold)
                },
                text = {
                    OutlinedTextField(
                        value = newPostText,
                        onValueChange = { newPostText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = { Text("Share something with the community") },
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50)
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun PostCard(post: Post) {
    var isLiked by remember { mutableStateOf(post.isLiked) }
    var isBookmarked by remember { mutableStateOf(post.isBookmarked) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.username.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.username,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = post.timeAgo,
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Content
            Text(
                text = post.content,
                fontSize = 14.sp,
                color = Color(0xFF424242),
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(12.dp))

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isLiked = !isLiked }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFF4CAF50) else Color(0xFF757575),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = post.likes.toString(),
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }

                // Comment
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "Comment",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = post.comments.toString(),
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }

                Spacer(Modifier.weight(1f))

                // Bookmark
                IconButton(
                    onClick = { isBookmarked = !isBookmarked },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Color(0xFF4CAF50) else Color(0xFF757575),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavItem(
                icon = Icons.Default.LocationOn,
                label = "Map",
                isSelected = currentScreen == "Map",
                onClick = { onNavigate("Map") }
            )
            NavItem(
                icon = Icons.Default.Group,
                label = "Community",
                isSelected = currentScreen == "Community",
                onClick = { onNavigate("Community") }
            )
            NavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentScreen == "Profile",
                onClick = { onNavigate("Profile") }
            )
            NavItem(
                icon = Icons.Default.BarChart,
                label = "Progress",
                isSelected = currentScreen == "Progress",
                onClick = { onNavigate("Progress") }
            )
            NavItem(
                icon = Icons.Default.CloudDownload,
                label = "Offline",
                isSelected = currentScreen == "Offline",
                onClick = { onNavigate("Offline") }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF4CAF50) else Color(0xFF9E9E9E)
        )
    }
}