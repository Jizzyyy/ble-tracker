package com.bletracker.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bletracker.ui.theme.PrimaryBlue
import com.bletracker.ui.theme.TextPrimary
import com.bletracker.ui.theme.TextSecondary
import com.bletracker.ui.theme.ZoneMid
import com.bletracker.ui.theme.ZoneVeryWeak

object BlePermissionUtils {
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    fun hasPermissions(context: Context): Boolean {
        return getRequiredPermissions().all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isLocationEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return true
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
            lm?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }
}

@Composable
fun BleRequirementCard(
    isBluetoothEnabled: Boolean,
    onEnableBluetooth: () -> Unit,
    onPermissionsGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermissions by remember {
        mutableStateOf(BlePermissionUtils.hasPermissions(context))
    }
    var isLocationEnabled by remember {
        mutableStateOf(BlePermissionUtils.isLocationEnabled(context))
    }

    // Refresh instantly on Resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermissions = BlePermissionUtils.hasPermissions(context)
                isLocationEnabled = BlePermissionUtils.isLocationEnabled(context)
                if (hasPermissions) {
                    onPermissionsGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        hasPermissions = granted
        if (granted) {
            onPermissionsGranted()
        }
    }

    if (!hasPermissions) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFEF2F2))
                .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = ZoneVeryWeak,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Izin Pemindaian Diperlukan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            "Aplikasi memerlukan izin Bluetooth Scan & Nearby Devices untuk mendeteksi sinyal BLE."
                        } else {
                            "Aplikasi memerlukan izin Lokasi untuk mendeteksi beacon Bluetooth di Android ini."
                        },
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Button(
                            onClick = {
                                permissionLauncher.launch(BlePermissionUtils.getRequiredPermissions())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ZoneVeryWeak),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Izinkan Akses", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Buka Pengaturan", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    } else if (!isBluetoothEnabled) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFBEB))
                .border(1.dp, Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.BluetoothDisabled,
                    contentDescription = null,
                    tint = ZoneMid,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bluetooth Tidak Aktif",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Aktifkan Bluetooth untuk memulai pencarian dan melacak perangkat.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onEnableBluetooth,
                        colors = ButtonDefaults.buttonColors(containerColor = ZoneMid),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Aktifkan Bluetooth", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    } else if (!isLocationEnabled && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFFBEB))
                .border(1.dp, Color(0xFFFEF3C7), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = null,
                    tint = ZoneMid,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GPS / Layanan Lokasi Perlu Aktif",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pada Android 11 ke bawah, sistem Android mewajibkan layanan lokasi aktif untuk menerima data sinyal BLE.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ZoneMid),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Buka Pengaturan Lokasi", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
