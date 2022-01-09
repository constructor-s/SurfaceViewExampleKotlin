package com.shirunjie.surfaceviewexample

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.SurfaceHolder
import android.graphics.RectF
import kotlin.math.floor
import android.graphics.BitmapFactory
import android.os.Build
import android.view.MotionEvent


class GameView(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), Runnable {
    private val mContext = context
    private val mSurfaceHolder = holder
    private val mPaint = Paint().apply { color = Color.DKGRAY }
    private val mPath = Path()
    private var mBitmapX = 0f
    private var mBitmapY = 0f
    private var mViewWidth = 0
    private var mViewHeight = 0
    private var mWinnerRect = RectF()
    private lateinit var mBitmap: Bitmap
    private var mRunning = false
    private lateinit var mGameThread: Thread
    private lateinit var mFlashlightCone: FlashlightCone

    override fun run() {
        var canvas: Canvas
        while (mRunning) {
            if (mSurfaceHolder.surface.isValid) {
                val x: Float = mFlashlightCone.x.toFloat()
                val y: Float = mFlashlightCone.y.toFloat()
                val radius: Float = mFlashlightCone.radius.toFloat()

                // Lock the canvas. Note that in a more complex app, with
                // more threads, you need to put this into a try/catch block
                // to make sure only one thread is drawing to the surface.
                // Starting with O, you can request a hardware surface with
                //    lockHardwareCanvas().
                // See https://developer.android.com/reference/android/view/
                //    SurfaceHolder.html#lockHardwareCanvas()
                canvas = mSurfaceHolder.lockCanvas()
                // canvas = mSurfaceHolder.lockHardwareCanvas()

                // Fill the canvas with white and draw the bitmap.
                canvas.save()
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(mBitmap, mBitmapX, mBitmapY, mPaint)

                // Add clipping region and fill rest of the canvas with black.
                mPath.addCircle(x, y, radius, Path.Direction.CCW);

                // The method clipPath(path, Region.Op.DIFFERENCE) was
                // deprecated in API level 26. The recommended alternative
                // method is clipOutPath(Path), which is currently available
                // in API level 26 and higher.
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    canvas.clipPath(mPath, Region.Op.DIFFERENCE);
                } else {
                    canvas.clipOutPath(mPath);
                }

                canvas.drawColor(Color.BLACK);

                // If the x, y coordinates of the user touch are within a
                //  bounding rectangle, display the winning message.
                if (x > mWinnerRect.left && x < mWinnerRect.right
                    && y > mWinnerRect.top && y < mWinnerRect.bottom) {
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(mBitmap, mBitmapX, mBitmapY, mPaint);
                    canvas.drawText(
                        "WIN!", mViewWidth / 3.0f, mViewHeight / 2.0f, mPaint);
                }
                // Clear the path data structure.
                mPath.rewind();
                // Restore the previously saved (default) clip and matrix state.
                canvas.restore();
                // Release the lock on the canvas and show the surface's
                // contents on the screen.
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                setUpBitmap()
                // Set coordinates of flashlight cone.
                updateFrame(x.toInt(), y.toInt())
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                // Updated coordinates for flashlight cone.
                updateFrame(x.toInt(), y.toInt())
                invalidate()
            }
            else -> {}
        }
        return true
    }

    private fun updateFrame(newX: Int, newY: Int) {
        mFlashlightCone.update(newX, newY)
    }

    fun resume() {
        mRunning = true
        mGameThread = Thread(this)
        mGameThread.start()
    }

    fun pause() {
        mRunning = false
        try {
            // Stop the thread (rejoin the main thread)
            mGameThread.join()
        } catch (e: InterruptedException) {
        }
    }

    /**
     * We cannot get the correct dimensions of views in onCreate because
     * they have not been inflated yet. This method is called every time the
     * size of a view changes, including the first time after it has been
     * inflated.
     *
     * @param w Current width of view.
     * @param h Current height of view.
     * @param oldw Previous width of view.
     * @param oldh Previous height of view.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
        mFlashlightCone = FlashlightCone(mViewWidth, mViewHeight)

        // Set font size proportional to view size.
        mPaint.textSize = (mViewHeight / 5).toFloat()
        mBitmap = BitmapFactory.decodeResource(
            mContext!!.resources, R.drawable.android
        )
        setUpBitmap()
    }


    /**
     * Calculates a randomized location for the bitmap
     * and the winning bounding rectangle.
     */
    private fun setUpBitmap() {
        mBitmapX = floor(
            Math.random().toFloat() * (mViewWidth - mBitmap.width)
        )
        mBitmapY = floor(
            Math.random().toFloat() * (mViewHeight - mBitmap.height)
        )
        mWinnerRect = RectF(
            mBitmapX.toFloat(), mBitmapY.toFloat(),
            mBitmapX + mBitmap.width,
            mBitmapY + mBitmap.height
        )
    }
}