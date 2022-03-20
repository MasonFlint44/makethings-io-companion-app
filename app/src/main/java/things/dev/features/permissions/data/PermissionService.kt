package things.dev.features.permissions.data

interface PermissionService {
    fun ensurePermission(permission: String)
}