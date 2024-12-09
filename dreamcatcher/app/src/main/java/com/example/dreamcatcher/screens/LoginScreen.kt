import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.dreamcatcher.MainActivity.Companion.RC_SIGN_IN
import com.example.dreamcatcher.MainViewModel
import com.example.dreamcatcher.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit,viewModel: MainViewModel) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val googleSignInClient = remember { getGoogleSignInClient(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login/Register", style = androidx.compose.material.MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and password must not be empty"
                    return@Button
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser

                            firebaseUser?.let { user ->
                                // 同步 Firebase 用户到本地数据库
                                viewModel.syncFirebaseUserWithLocalData(user)

                                Toast.makeText(context, "Registered Successfully!", Toast.LENGTH_SHORT).show()

                                onLoginSuccess()}
                        } else {
                            errorMessage = task.exception?.message
                        }
                    }
            }) {
                Text("Register")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and password must not be empty"
                    return@Button
                }

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            firebaseUser?.let { user ->
                                viewModel.syncFirebaseUserWithLocalData(user)

                                Toast.makeText(context, "Logged in Successfully!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()}
                        } else {
                            errorMessage = task.exception?.message
                        }
                    }
            }) {
                Text("Login")
            }
        }
        Button(onClick = {
            val signInIntent = googleSignInClient.signInIntent
            (context as ComponentActivity).startActivityForResult(signInIntent, RC_SIGN_IN)
        }) {
            Text("Login with Google")
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = androidx.compose.ui.graphics.Color.Red)
        }
    }
}

fun getGoogleSignInClient(context: Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    return GoogleSignIn.getClient(context, gso)
}