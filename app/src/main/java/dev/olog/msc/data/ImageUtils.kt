package dev.olog.msc.data

import android.graphics.*
import dev.olog.msc.utils.k.extension.shuffle
import dev.olog.shared_android.assertBackgroundThread


object ImageUtils {

    private const val IMAGE_SIZE = 1500

    fun joinImages(list: List<Bitmap>) : Bitmap {
        assertBackgroundThread()

        list.shuffle()
        val resultList = arrangeBitmaps(list)

        val combinedImage = create(resultList, IMAGE_SIZE, 3)
        return rotateAndCrop(combinedImage, IMAGE_SIZE, 9f)
    }

    private fun arrangeBitmaps(list: List<Bitmap>): List<Bitmap> {
        return when {
            list.size == 1 -> {
                val item = list[0]
                listOf(item, item, item, item, item, item, item, item, item)
            }
            list.size == 2 -> {
                val item1 = list[0]
                val item2 = list[1]
                listOf(item1, item2, item1, item2, item1, item2, item1, item2, item1)
            }
            list.size == 3 -> {
                val item1 = list[0]
                val item2 = list[1]
                val item3 = list[2]
                listOf(item1, item2, item3, item3, item1, item2, item2, item3, item1)
            }
            list.size == 4 -> {
                val item1 = list[0]
                val item2 = list[1]
                val item3 = list[2]
                val item4 = list[3]
                listOf(item1, item2, item3, item4, item1, item2, item3, item4, item1)
            }
            list.size < 9 -> { // 5 to 8
                val item1 = list[0]
                val item2 = list[1]
                val item3 = list[2]
                val item4 = list[3]
                val item5 = list[4]
                listOf(item1, item2, item3, item4, item5, item2, item3, item4, item1)
            }
            else -> list // case 9
        }
    }

    private fun create(images: List<Bitmap>, imageSize: Int, parts: Int) : Bitmap {
        val result = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val onePartSize = imageSize / parts

        images.forEachIndexed { i, bitmap ->
            val bit = Bitmap.createScaledBitmap(bitmap, onePartSize, onePartSize, false)
            canvas.drawBitmap(bit, (onePartSize * (i % parts)).toFloat(), (onePartSize * (i / parts)).toFloat(), paint)
        }

        paint.color = Color.WHITE
        paint.strokeWidth = 10f

        val oneThirdSize = (IMAGE_SIZE / 3).toFloat()
        val twoThirdSize = (IMAGE_SIZE / 3 * 2).toFloat()
        // vertical lines
        canvas.drawLine(oneThirdSize, 0f, oneThirdSize, imageSize.toFloat(), paint)
        canvas.drawLine(twoThirdSize,0f, twoThirdSize, imageSize.toFloat(), paint)
        // horizontal lines
        canvas.drawLine(0f, oneThirdSize, imageSize.toFloat(), oneThirdSize, paint)
        canvas.drawLine(0f, twoThirdSize, imageSize.toFloat(), twoThirdSize, paint)

        return result
    }

    private fun rotateAndCrop(bitmap: Bitmap, imageSize: Int, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)

        val rotated = Bitmap.createBitmap(bitmap, 0, 0, imageSize, imageSize, matrix, true)
        bitmap.recycle()
        val cropStart = imageSize * 25 / 100
        val cropEnd : Int = (cropStart * 1.5).toInt()
        val cropped = Bitmap.createBitmap(rotated, cropStart, cropStart, imageSize - cropEnd, imageSize - cropEnd)
        rotated.recycle()

        return cropped
    }


}