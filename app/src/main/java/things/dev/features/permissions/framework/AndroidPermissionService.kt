package things.dev.features.permissions.framework

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import javax.inject.Inject

class AndroidPermissionService @Inject constructor(private val activity: Activity): PermissionService {
    override fun ensurePermission(permission: String) {
        if (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        ActivityCompat.requestPermissions(activity, arrayOf(permission), 14546)
    }
}