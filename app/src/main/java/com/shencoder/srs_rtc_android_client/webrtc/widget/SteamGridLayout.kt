package com.shencoder.srs_rtc_android_client.webrtc.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.core.view.isGone
import com.shencoder.srs_rtc_android_client.R
import com.shencoder.srs_rtc_android_client.webrtc.bean.WebRTCStreamInfoBean
import org.webrtc.EglBase
import org.webrtc.PeerConnectionFactory

/**
 * 专门用来摆放[BaseStreamSurfaceViewRenderer]，不可add其他view
 * 仅可以放一个[PublishStreamSurfaceViewRenderer]，可以放多个[PlayStreamSurfaceViewRenderer]
 * @see [getColumns]
 *
 * @author  ShenBen
 * @date    2022/01/22 12:33
 * @email   714081644@qq.com
 */
class SteamGridLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    companion object {
        const val DEFAULT_PIP_MODE = false
        const val DEFAULT_PIP_PERCENT = 0.25f
        const val DEFAULT_PIP_MARGIN = 20

    }

    /**
     * 画中画模式
     * picture-in-picture mode
     *
     * 仅在私聊时有作用，一推一拉
     */
    private var pipMode: Boolean

    /**
     * 画中画view的宽高占父布局的宽高比
     */
    private var pipPercent: Float
    private var pipMarginTop: Int
    private var pipMarginEnd: Int

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var sharedContext: EglBase.Context

    init {
        val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.SteamGridLayout)
        pipMode = typedArray.getBoolean(R.styleable.SteamGridLayout_sgl_pip_mode, DEFAULT_PIP_MODE)
        pipPercent =
            typedArray.getFloat(R.styleable.SteamGridLayout_sgl_pip_percent, DEFAULT_PIP_PERCENT)
        pipMarginTop =
            typedArray.getDimensionPixelSize(
                R.styleable.SteamGridLayout_sgl_pip_margin_top,
                DEFAULT_PIP_MARGIN
            )
        pipMarginEnd =
            typedArray.getDimensionPixelSize(
                R.styleable.SteamGridLayout_sgl_pip_margin_end,
                DEFAULT_PIP_MARGIN
            )
        typedArray.recycle()
        isChildrenDrawingOrderEnabled = true
    }

    fun init(peerConnectionFactory: PeerConnectionFactory, sharedContext: EglBase.Context) {
        this.peerConnectionFactory = peerConnectionFactory
        this.sharedContext = sharedContext
    }

    fun setPipMode(isPipMode: Boolean) {
        if (pipMode != isPipMode) {
            pipMode = isPipMode
            if (childCount == 2) {
                requestLayout()
            }
        }
    }

    fun setPipMarginPercent(@FloatRange(from = 0.0, to = 1.0) pipPercent: Float) {
        if (this.pipPercent != pipPercent) {
            this.pipPercent = pipPercent
            if (pipMode && childCount == 2) {
                requestLayout()
            }
        }
    }

    fun setPipMarginTop(@Px pipMarginTop: Int) {
        if (this.pipMarginTop != pipMarginTop) {
            this.pipMarginTop = pipMarginTop
            if (pipMode && childCount == 2) {
                requestLayout()
            }
        }
    }

    fun setPipMarginEnd(@Px pipMarginEnd: Int) {
        if (this.pipMarginEnd != pipMarginEnd) {
            this.pipMarginEnd = pipMarginEnd
            if (pipMode && childCount == 2) {
                requestLayout()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            throw RuntimeException("layout width or height can't be unspecified")
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val childCount = childCount
        if (childCount > 0) {
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)

            val realWidth = widthSize - paddingStart - paddingEnd
            val realHeight = heightSize - paddingTop - paddingBottom

            if (childCount == 1) {
                val childView = getChildAt(0)
                val layoutParams = childView.layoutParams as LayoutParams
                childView.measure(
                    MeasureSpec.makeMeasureSpec(
                        realWidth - layoutParams.marginStart - layoutParams.marginEnd,
                        MeasureSpec.EXACTLY
                    ),
                    MeasureSpec.makeMeasureSpec(
                        realHeight - layoutParams.topMargin - layoutParams.bottomMargin,
                        MeasureSpec.EXACTLY
                    )
                )
            } else {
                if (childCount == 2) {
                    val childAt1 = getChildAt(0)
                    val childAt2 = getChildAt(1)

                    var publishView: View? = null
                    var playView: View? = null
                    if (childAt1 is PublishStreamSurfaceViewRenderer) {
                        publishView = childAt1
                        playView = childAt2
                    } else if (childAt2 is PublishStreamSurfaceViewRenderer) {
                        publishView = childAt2
                        playView = childAt1
                    }

                    if (publishView != null && playView != null && pipMode) {
                        //有推流画面，并且是画中画模式
                        //拉流画面铺满
                        val playLayoutParams = playView.layoutParams as LayoutParams
                        playView.measure(
                            MeasureSpec.makeMeasureSpec(
                                realWidth - playLayoutParams.marginStart - playLayoutParams.marginEnd,
                                MeasureSpec.EXACTLY
                            ),
                            MeasureSpec.makeMeasureSpec(
                                realHeight - playLayoutParams.topMargin - playLayoutParams.bottomMargin,
                                MeasureSpec.EXACTLY
                            )
                        )
                        val publishLayoutParams = playView.layoutParams as LayoutParams
                        //推流画面宽高是百分比
                        publishView.measure(
                            MeasureSpec.makeMeasureSpec(
                                (realWidth * pipPercent).toInt() - publishLayoutParams.marginStart - publishLayoutParams.marginEnd,
                                MeasureSpec.EXACTLY
                            ),
                            MeasureSpec.makeMeasureSpec(
                                (realHeight * pipPercent).toInt() - publishLayoutParams.topMargin - publishLayoutParams.bottomMargin,
                                MeasureSpec.EXACTLY
                            )
                        )
                        return
                    }
                }
                val columns = getColumns(childCount)
                val itemWidth = realWidth / columns
                //正方形view
                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    val layoutParams = childView.layoutParams as LayoutParams
                    childView.measure(
                        MeasureSpec.makeMeasureSpec(
                            itemWidth - layoutParams.marginStart - layoutParams.marginEnd,
                            MeasureSpec.EXACTLY
                        ),
                        MeasureSpec.makeMeasureSpec(
                            itemWidth - layoutParams.topMargin - layoutParams.bottomMargin,
                            MeasureSpec.EXACTLY
                        )
                    )
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val childCount = childCount
        if (childCount > 0) {
            val parentWidth = right - left
            val parentHeight = bottom - top
            val parentStart = paddingStart
            val parentTop = paddingTop
            val parentEnd = parentWidth - paddingEnd
            val parentBottom = parentHeight - paddingBottom

            var startX = parentStart
            var startY = parentTop

            if (childCount == 1) {
                val childView = getChildAt(0)
                if (childView.isGone.not()) {
                    val layoutParams = childView.layoutParams as LayoutParams
                    childView.layout(
                        startX + layoutParams.marginStart,
                        startY + layoutParams.topMargin,
                        startX + layoutParams.marginStart + childView.measuredWidth,
                        startY + layoutParams.topMargin + childView.measuredHeight
                    )
                }
            } else {
                if (childCount == 2) {
                    val childAt1 = getChildAt(0)
                    val childAt2 = getChildAt(1)

                    var publishView: View? = null
                    var playView: View? = null
                    if (childAt1 is PublishStreamSurfaceViewRenderer) {
                        publishView = childAt1
                        playView = childAt2
                    } else if (childAt2 is PublishStreamSurfaceViewRenderer) {
                        publishView = childAt2
                        playView = childAt1
                    }

                    if (publishView != null && playView != null && pipMode) {
                        if (publishView.isGone.not()) {
                            val layoutParams = publishView.layoutParams as LayoutParams
                            //布局在右上角
                            val childLeft =
                                parentEnd - pipMarginEnd - layoutParams.marginEnd - publishView.measuredWidth
                            val childTop = parentTop + pipMarginTop + layoutParams.topMargin
                            val childRight = childLeft + publishView.measuredWidth
                            val childBottom = childTop + publishView.measuredHeight
                            publishView.layout(childLeft, childTop, childRight, childBottom)
                        }
                        if (playView.isGone.not()) {
                            val layoutParams = playView.layoutParams as LayoutParams
                            //拉流画面铺满
                            playView.layout(
                                startX + layoutParams.marginStart,
                                startY + layoutParams.topMargin,
                                startX + layoutParams.marginStart + playView.measuredWidth,
                                startY + layoutParams.topMargin + playView.measuredHeight
                            )
                        }
                        return
                    }
                }

                val childWidthSpace: Int = parentWidth - paddingStart - paddingEnd
                val childHeightSpace: Int = parentHeight - paddingTop - paddingBottom

                for (i in 0 until childCount) {
                    val childView = getChildAt(i)
                    if (childView.isGone) {
                        continue
                    }

                    val layoutParams = childView.layoutParams as LayoutParams
                    val childWidth = childView.measuredWidth
                    val childHeight = childView.measuredHeight
                    if (childWidthSpace < startX + childWidth + layoutParams.marginStart + layoutParams.marginEnd) {
                        //换行
                        startX = parentStart
                        startY += childHeight + layoutParams.topMargin + layoutParams.bottomMargin
                        if (startY > childHeightSpace) {
                            //超出父布局高度，不继续绘制了
                            break
                        }
                    }

                    val childLeft = startX + layoutParams.marginStart
                    val childTop = startY + layoutParams.topMargin
                    val childRight = childLeft + childWidth
                    val childBottom = childTop + childHeight

                    childView.layout(
                        childLeft,
                        childTop,
                        childRight,
                        childBottom
                    )
                    startX += childWidth + layoutParams.marginStart + layoutParams.marginEnd
                }
            }
        }

    }

    override fun getChildDrawingOrder(childCount: Int, drawingPosition: Int): Int {
        if (childCount == 2) {
            //画中画模式重置绘制顺序
            val childAt1 = getChildAt(0)
            if (pipMode && childAt1 is PublishStreamSurfaceViewRenderer) {
                return if (drawingPosition == 0) {
                    1
                } else {
                    0
                }
            }
        }
        return super.getChildDrawingOrder(childCount, drawingPosition)
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return when (p) {
            is LayoutParams -> LayoutParams(p)
            is MarginLayoutParams -> LayoutParams(p)
            else -> LayoutParams(p)
        }
    }

    override fun onViewAdded(child: View) {
        checkView(child)
        if (this::peerConnectionFactory.isInitialized && this::sharedContext.isInitialized) {
            (child as BaseStreamSurfaceViewRenderer).init(peerConnectionFactory, sharedContext)
        }
    }

    override fun onViewRemoved(child: View) {
        //释放资源
        if (child is BaseStreamSurfaceViewRenderer) {
            child.release()
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        val childCount = childCount
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView is BaseStreamSurfaceViewRenderer) {
                childView.release()
            }
        }
    }

    private fun checkView(child: View) {
        check(child is BaseStreamSurfaceViewRenderer) {
            "child must be BaseStreamSurfaceViewRenderer."
        }
        //仅可以添加一个PublishStreamSurfaceViewRenderer
        if (child is PublishStreamSurfaceViewRenderer) {
            val childCount = childCount
            var publishCount = 0
            for (i in 0 until childCount) {
                if (getChildAt(i) is PublishStreamSurfaceViewRenderer) {
                    ++publishCount
                }
            }
            if (publishCount > 1) {
                throw IllegalStateException("A PublishStreamSurfaceViewRenderer already exists.")
            }
        }
    }

    fun getPublishStreamSurfaceViewRenderer(): PublishStreamSurfaceViewRenderer? {
        val childCount = childCount
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            if (childAt is PublishStreamSurfaceViewRenderer) {
                return childAt
            }
        }
        return null
    }

    fun getAllPlayStreamSurfaceViewRenderer(): List<PlayStreamSurfaceViewRenderer> {
        val list = mutableListOf<PlayStreamSurfaceViewRenderer>()
        val childCount = childCount
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            if (childAt is PlayStreamSurfaceViewRenderer) {
                list.add(childAt)
            }
        }
        return list
    }

    fun getPlayStreamSurfaceViewRenderer(
        userId: String,
        userType: String
    ): PlayStreamSurfaceViewRenderer? {
        val bean = WebRTCStreamInfoBean(userId, userType)
        val childCount = childCount
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            if (childAt is PlayStreamSurfaceViewRenderer) {
                if (bean == childAt.webrtcStreamInfoBean) {
                    return childAt
                }
            }
        }
        return null
    }

    fun removePlayStreamSurfaceView(userId: String, userType: String) {
        val bean = WebRTCStreamInfoBean(userId, userType)
        val childCount = childCount
        for (i in 0 until childCount) {
            val childAt = getChildAt(i)
            if (childAt is PlayStreamSurfaceViewRenderer) {
                if (bean == childAt.webrtcStreamInfoBean) {
                    removeViewAt(i)
                    break
                }
            }
        }
    }

    private fun getColumns(childCount: Int): Int {
        return when (childCount) {
            1 -> 1
            in 2..4 -> 2
            in 5..9 -> 3
            in 10..16 -> 4
            else -> 5
        }
    }

    open class LayoutParams : MarginLayoutParams {

        /**
         * view的边境位置
         */
        val mBorderRect = Rect()

        /**
         * 用于实际绘制位置信息
         */
        val mDecorInsets = Rect()

        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs) {}
        constructor(width: Int, height: Int) : super(width, height) {}
        constructor(source: MarginLayoutParams?) : super(source) {}
        constructor(source: ViewGroup.LayoutParams?) : super(source) {}
        constructor(source: LayoutParams?) : super(source as ViewGroup.LayoutParams?) {}
    }
}