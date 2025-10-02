package com.renova.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.renova.mobile.R
import com.renova.mobile.ui.theme.*

@Composable
fun TermsAndConditionsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.renovaColors

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Términos y Condiciones",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "Cerrar",
                                tint = colors.iconTint
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = colors.textSecondary.copy(alpha = 0.3f)
                    )

                    // Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Términos de Servicio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RenovaColors.Primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = """
RENOVA - Marco Legal y Protección de Datos

QUIÉNES SOMOS

Renova es una plataforma de tecnología sostenible que promueve el reciclaje a través de depósitos inteligentes, una aplicación móvil y un sitio web. Nuestro objetivo es incentivar la economía circular, otorgando recompensas a los usuarios que participan activamente en la correcta disposición de materiales reciclables.

Nos comprometemos a operar con transparencia, responsabilidad social y apego a la legislación mexicana en materia de protección de datos personales.

CONDICIONES Y TÉRMINOS DE SERVICIO

Al registrarse y utilizar Renova (aplicación móvil, sitio web o depósitos inteligentes), el usuario acepta los siguientes términos:

1. Uso adecuado de la plataforma:
El usuario se compromete a proporcionar información veraz y a utilizar el sistema únicamente para los fines autorizados (reciclaje, acumulación de puntos, canje de recompensas).

2. Cuenta y credenciales:
El usuario es responsable de la confidencialidad de sus credenciales de acceso. Renova no se hace responsable de accesos indebidos ocasionados por negligencia del usuario.

3. Recompensas y puntos:
Los puntos acumulados no constituyen dinero en efectivo, salvo que se especifique lo contrario en las promociones activas. Renova podrá modificar las reglas de acumulación y canje con previo aviso.

4. Disponibilidad del servicio:
Aunque Renova busca garantizar la continuidad de la plataforma, no se responsabiliza por interrupciones causadas por fallas técnicas, mantenimientos programados o factores externos.

5. Prohibiciones:
Está prohibido manipular los depósitos inteligentes, registrar información falsa o realizar actividades fraudulentas que afecten al sistema o a otros usuarios.

6. Aceptación de políticas:
El uso de Renova implica la aceptación plena de la presente política de privacidad y seguridad, así como de las futuras actualizaciones que se comuniquen al usuario.

POLÍTICA DE PRIVACIDAD Y USO DE DATOS

Introducción:
En Renova, protegemos la privacidad de nuestros usuarios y tratamos sus datos personales con responsabilidad. Esta política explica cómo recopilamos, utilizamos, almacenamos y compartimos la información.

Datos que recopilamos:
- Datos de identidad: copia de INE, fotografía tipo selfie para validación.
- Datos de contacto: nombre, correo electrónico, número telefónico.
- Datos de uso: historial de reciclaje, puntos acumulados, transacciones y recompensas.
- Datos técnicos: dirección IP, dispositivo, registros de actividad en la app.
- Datos de ubicación: únicamente al momento de usar un depósito inteligente.

Finalidad del tratamiento:
- Validar identidad y acceso a los servicios.
- Permitir el uso de depósitos inteligentes y aplicación.
- Gestionar puntos y recompensas.
- Emitir reportes ambientales.
- Mejorar la experiencia de usuario.
- Cumplir obligaciones legales.

Consentimiento:
El usuario otorga su consentimiento expreso al registrarse y aceptar esta política. Puede revocar su consentimiento en cualquier momento solicitándolo por correo, lo cual puede implicar la suspensión del servicio.

Seguridad de la información:
Renova aplica medidas técnicas y organizativas para proteger los datos:
- Cifrado de datos en tránsito y reposo.
- Autenticación segura de administradores.
- Monitoreo y auditorías periódicas.

Transferencia de datos:
Los datos no se comparten con terceros, salvo:
- Proveedores de servicios que apoyan la operación (ej. pagos, almacenamiento en nube).
- Requerimientos legales o regulatorios.
- Procesos de fusión o reestructuración empresarial.

Derechos del usuario (ARCO):
Conforme a la Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP), el usuario puede:
- Acceder a sus datos.
- Rectificarlos si son incorrectos.
- Cancelarlos cuando lo desee.
- Oponerse a su tratamiento.

Las solicitudes deben enviarse a: soyrenovaapp@gmail.com

Conservación:
Los datos se conservarán únicamente el tiempo necesario para cumplir con los fines descritos y de acuerdo con la legislación aplicable.

Cambios en la política:
Podemos actualizar esta política en cualquier momento. Los cambios se comunicarán en la app o al correo registrado. El uso continuado implica aceptación de la versión vigente.

CONTÁCTANOS

Si tienes preguntas o deseas ejercer tus derechos de privacidad, contáctanos en:

📧 soyrenovaapp@gmail.com
📍 Manzanillo, Colima, México
                            """.trimIndent(),
                            fontSize = 14.sp,
                            color = colors.textPrimary,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Política de Privacidad",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RenovaColors.Primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = """
                                1. Información que recopilamos
                                
                                Recopilamos información que usted nos proporciona directamente, como nombre, correo electrónico y datos de uso de la aplicación.
                                
                                2. Uso de la información
                                
                                Utilizamos la información recopilada para proporcionar, mantener y mejorar nuestros servicios, así como para comunicarnos con usted.
                                
                                3. Compartir información
                                
                                No compartimos su información personal con terceros sin su consentimiento, excepto cuando sea necesario para proporcionar nuestros servicios.
                                
                                4. Seguridad
                                
                                Implementamos medidas de seguridad para proteger su información personal contra acceso no autorizado y uso indebido.
                                
                                5. Sus derechos
                                
                                Usted tiene derecho a acceder, corregir o eliminar su información personal en cualquier momento.
                            """.trimIndent(),
                            fontSize = 14.sp,
                            color = colors.textPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}