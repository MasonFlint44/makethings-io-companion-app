package things.dev.features.permissions.framework

interface PermissionService {
    fun ensurePermission(permission: String)
}