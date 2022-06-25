package de.datlag.model.burningseries

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.util.Base64
import androidx.room.*
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

@Parcelize
@Serializable
@Entity(
    tableName = "CoverTable",
    indices = [
        Index("coverId"),
        Index("href", unique = true)
    ]
)
@Obfuscate
data class Cover(
    @ColumnInfo(name = "href") @SerialName("href") val href: String = String(),
    @ColumnInfo(name = "blurHash") @SerialName("blurHash") val blurHash: String = String(),
    @Ignore @SerialName("base64") val base64: String,
) : Parcelable {

    @IgnoredOnParcel
    @Transient
    @Ignore
    private var base64Image: Bitmap? = null

    @IgnoredOnParcel
    @Transient
    @Ignore
    private var blurHashDrawable: Drawable? = null

    @PrimaryKey(autoGenerate = true)
    @IgnoredOnParcel
    @ColumnInfo(name = "coverId")
    var coverId: Long = 0L

    constructor(
        href: String = String(),
        blurHash: String = String(),
    ) : this(href, blurHash, String())

    fun isDefault(): Boolean = this.hashCode() == Cover().hashCode()

    fun loadBase64Image(coversDir: File): Bitmap? {
        return if (base64Image != null) {
            base64Image
        } else {
            val file = File(coversDir, href.substringAfterLast('/'))
            val base64FromFile = if (file.exists() && file.canRead()) {
                try {
                    file.readText()
                } catch (ignored: Throwable) {
                    null
                }
            } else {
                null
            }
            val decoded = try {
                Base64.decode(base64FromFile ?: base64, Base64.DEFAULT)
            } catch (ignored: Throwable) {
                null
            }
            decoded?.let {
                (try {
                    BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                } catch (ignored: Throwable) {
                    null
                }).also { base64Image = it }
            }
        }
    }

    fun loadBlurHash(loadIfNotPresent: () -> Drawable?): Drawable? {
        return blurHashDrawable ?: loadIfNotPresent.invoke().also { blurHashDrawable = it }
    }
}
