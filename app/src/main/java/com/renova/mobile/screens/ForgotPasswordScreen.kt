package com.renova.mobile.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.renova.mobile.R
import com.renova.mobile.viewmodel.ForgotPasswordViewModel
import com.renova.mobile.ui.theme.*

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val colors = MaterialTheme.renovaColors

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var emailEmptyError by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val forgotPasswordState by viewModel.forgotPasswordState.collectAsState()

    LaunchedEffect(forgotPasswordState) {
        when {
            forgotPasswordState.isSuccess -> {
                showSuccessDialog = true
                showErrorDialog = false
            }

            forgotPasswordState.errorResId != null -> {
                showErrorDialog = true
                showSuccessDialog = false
            }

            else -> {
                showSuccessDialog = false
                showErrorDialog = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RenovaGradients.backgroundGradient())
            .padding(24.dp)
    ) {
        SubtleLeavesBackground(
            modifier = Modifier.fillMaxSize(),
            leafPositions = listOf(
                LeafPosition(R.drawable.leaf1, Alignment.BottomStart),
                LeafPosition(R.drawable.leaf2, Alignment.TopEnd)
            )
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = stringResource(R.string.logo_description),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(bottom = 24.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.forgot_password_title),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFontFamily,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.enter_email_password),
                            fontSize = 14.sp,
                            fontFamily = PoppinsFontFamily,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = stringResource(R.string.email),
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = PoppinsFontFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = it.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches()
                                if (it.isNotEmpty()) emailEmptyError = false
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_email),
                                    contentDescription = stringResource(R.string.email),
                                    tint = colors.iconTint
                                )
                            },
                            isError = emailError || emailEmptyError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = TextStyle(
                                color = colors.textPrimary,
                                fontFamily = PoppinsFontFamily
                            ),
                            colors = RenovaComponentColors.textFieldColors()
                        )

                        if ((emailError && email.isNotEmpty()) || emailEmptyError) {
                            Text(
                                text = if (emailEmptyError)
                                    stringResource(R.string.email_required)
                                else
                                    stringResource(R.string.email_invalid),
                                color = RenovaColors.Error,
                                fontSize = 12.sp,
                                fontFamily = PoppinsFontFamily,
                                lineHeight = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                emailEmptyError = email.isEmpty()
                                if (!emailEmptyError) {
                                    emailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                                    if (!emailError) {
                                        viewModel.forgotPassword(email)
                                    }
                                }
                            },
                            enabled = !forgotPasswordState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomGreenColor,
                                disabledContainerColor = CustomGreenColor.copy(alpha = 0.6f)
                            )
                        ) {
                            if (forgotPasswordState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.sending),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PoppinsFontFamily,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    stringResource(R.string.send_link),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PoppinsFontFamily,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = { onBackToLogin() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                stringResource(R.string.return_login),
                                color = RenovaColors.Primary,
                                fontSize = 14.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearState()
                onBackToLogin()
            },
            title = {
                Text(
                    stringResource(R.string.mail_sent),
                    color = RenovaColors.Success,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily
                )
            },
            text = {
                Text(
                    text = stringResource(forgotPasswordState.messageResId ?: R.string.recovery_link),
                    color = colors.textPrimary,
                    fontFamily = PoppinsFontFamily,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.clearState()
                        onBackToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RenovaColors.Success)
                ) {
                    Text(
                        stringResource(R.string.entendido),
                        color = Color.White,
                        fontFamily = PoppinsFontFamily
                    )
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                viewModel.clearState()
            },
            title = {
                Text(
                    stringResource(R.string.error_access),
                    color = RenovaColors.Error,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily
                )
            },
            text = {
                Text(
                    text = stringResource(forgotPasswordState.errorResId ?: R.string.error_unknown),
                    color = colors.textPrimary,
                    fontFamily = PoppinsFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        viewModel.clearState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RenovaColors.Error)
                ) {
                    Text(
                        stringResource(R.string.retry),
                        color = Color.White,
                        fontFamily = PoppinsFontFamily
                    )
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}