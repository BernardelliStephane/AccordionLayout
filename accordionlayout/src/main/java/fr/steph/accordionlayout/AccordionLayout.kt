package fr.steph.accordionlayout

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import fr.steph.accordionlayout.WidgetHelper.getFullHeight
import fr.steph.accordionlayout.WidgetHelper.isNullOrBlank

/**
 * Created by riyagayasen on 08/10/16.
 * Updated by Stéphane Bernardelli on 12/27/22
 */

class AccordionLayout : RelativeLayout {
    private val defaultTextColor = Color.parseColor("#737373")
    private lateinit var children: Array<View?>
    private lateinit var headingLayout: RelativeLayout
    private lateinit var heading: TextView
    private lateinit var partition: View
    private lateinit var body: RelativeLayout
    private lateinit var dropdownImage: ImageView
    private lateinit var dropupImage: ImageView
    private lateinit var headingImage: ImageView
    private lateinit var inflater: LayoutInflater
    private var expandCollapseListener: AccordionExpansionCollapseListener? = null

    private var isExpanded = false
    private var isAnimated = false
    private var isPartitioned = false

    private var headingString: String? = null
    private var headingColor = defaultTextColor
    private var headingTextSize = 0
    private var headingDrawable: Drawable? = null
    private var headingDrawableWidth = 0
    private var headingDrawableHeight = 0
    private var headingBackground: Drawable? = null
    private var headingBackgroundColor = Color.WHITE

    private var bodyTopMargin = 0
    private var bodyBottomMargin = 0
    private var bodyBackgroundColor = Color.WHITE
    private var bodyBackground: Drawable? = null

    /***
     * Constructor taking only the context. This is useful in case
     * the developer wants to programatically create an accordion view.
     *
     * @param context
     */
    constructor(context: Context) : super(context) {
        prepareLayoutWithoutChildren(context)
    }

    /***
     * The constructor taking an attribute set. This is called by the android OS itself,
     * in case this accordion component was included in the layout XML itself.
     * @param context
     * @param attrs
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        handleAttributeSet(context, attrs)
    }

    /***
     * Same as the constructor AccordionView(Context context, AttributeSet attrs)
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        handleAttributeSet(context, attrs)
    }

    /***
     * A function that takes the various attributes defined for this accordion. This accordion extends
     * a relative layout. There are certain custom attributes defined for this accordion whose values need
     * to be retrieved.
     * @param context
     * @param attrs
     */
    private fun handleAttributeSet(context: Context, attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(attrs, R.styleable.accordion, 0, 0).apply {
            try {
                isExpanded = getBoolean(R.styleable.accordion_isExpanded, false)
                isAnimated = getBoolean(R.styleable.accordion_isAnimated, false)
                isPartitioned = getBoolean(R.styleable.accordion_isPartitioned, false)
                headingString = getString(R.styleable.accordion_headingString)
                headingTextSize = getDimensionPixelSize(R.styleable.accordion_headingTextSize, 20)
                headingColor = getColor(R.styleable.accordion_headingColor, defaultTextColor)
                headingDrawable = getDrawable(R.styleable.accordion_headingDrawable)
                headingDrawableWidth =
                    getDimensionPixelSize(R.styleable.accordion_headingDrawableWidth, 0)
                headingDrawableHeight =
                    getDimensionPixelSize(R.styleable.accordion_headingDrawableHeight, 0)
                headingBackground = getDrawable(R.styleable.accordion_headingBackground)
                headingBackgroundColor = getColor(R.styleable.accordion_headingBackgroundColor, 0)
                bodyBackground = getDrawable(R.styleable.accordion_bodyBackground)
                bodyBackgroundColor = getColor(R.styleable.accordion_bodyBackgroundColor, 0)
            } finally {
                recycle()
            }
        }
    }

    /***
     * This creates an accordion layout. This is called when the user programatically creates an accordion.
     * 'Without Children' signifies that no UI elements have been added to the body of the accordion yet.
     * @param context
     */
    private fun initializeViewWithoutChildren(context: Context) {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val accordionLayout = inflater.inflate(R.layout.accordion, this, false) as LinearLayout
        headingLayout = accordionLayout.findViewById(R.id.heading_layout)
        heading = accordionLayout.findViewById(R.id.heading)
        partition = accordionLayout.findViewById(R.id.partition)
        body = accordionLayout.findViewById(R.id.body_layout)
        dropdownImage = accordionLayout.findViewById(R.id.dropdown_image)
        dropupImage = accordionLayout.findViewById(R.id.dropup_image)
        headingImage = accordionLayout.findViewById(R.id.heading_image)
        body.removeAllViews()
        removeAllViews()
        bodyBottomMargin = (body.layoutParams as LinearLayout.LayoutParams).bottomMargin
        bodyTopMargin = (body.layoutParams as LinearLayout.LayoutParams).topMargin
        addView(accordionLayout)
    }

    /***
     * This function is called when the accordion is added in the XML itself and is used to initialize
     * the various components of the accordion
     * @param context
     */
    private fun initializeViews(context: Context) {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val accordionLayout = inflater.inflate(R.layout.accordion, this, false) as LinearLayout
        headingLayout = accordionLayout.findViewById(R.id.heading_layout)
        heading = accordionLayout.findViewById(R.id.heading)
        partition = accordionLayout.findViewById(R.id.partition)
        body = accordionLayout.findViewById(R.id.body_layout)
        dropdownImage = accordionLayout.findViewById(R.id.dropdown_image)
        dropupImage = accordionLayout.findViewById(R.id.dropup_image)
        headingImage = accordionLayout.findViewById(R.id.heading_image)
        body.removeAllViews()
        children = arrayOfNulls(childCount)

        var i = 0
        while (i < childCount) children[i] = getChildAt(i++)

        removeAllViews()

        i = 0
        while (i < children.size) body.addView(children[i++])

        bodyBottomMargin = (body.layoutParams as LinearLayout.LayoutParams).bottomMargin
        bodyTopMargin = (body.layoutParams as LinearLayout.LayoutParams).topMargin
        addView(accordionLayout)
    }

    /***
     * This function, after initializing the accordion, performs necessary UI operations like setting
     * the partition or adding animation or expanding/collapsing the accordion
     * @param context
     */
    private fun prepareLayout(context: Context) {
        initializeViews(context)

        headingLayout.layoutTransition = if (isAnimated) LayoutTransition() else null
        heading.text = headingString
        heading.textSize = headingTextSize.toFloat()
        heading.setTextColor(headingColor)
        if (!isNullOrBlank(headingDrawable)) {
            headingImage.setImageDrawable(headingDrawable)
            if (headingDrawableWidth != 0) headingImage.layoutParams.width = headingDrawableWidth
            if (headingDrawableHeight != 0) headingImage.layoutParams.height = headingDrawableHeight
        }

        if (!isNullOrBlank(headingBackground)) headingLayout.background = headingBackground
        else if (!isNullOrBlank(headingBackgroundColor)) headingLayout.setBackgroundColor(
            headingBackgroundColor)

        partition.visibility = if (isPartitioned) VISIBLE else INVISIBLE

        body.visibility = VISIBLE
        if (!isNullOrBlank(bodyBackground)) body.background = bodyBackground
        else if (!isNullOrBlank(bodyBackgroundColor)) body.setBackgroundColor(bodyBackgroundColor)

        if (isExpanded) expand() else collapse()

        setOnClickListenerOnHeading()
    }

    /***
     * This function is used to prepare the layout after the initialize function but is called when the developer PROGRAMATICALLY adds
     * the accordion from the class. Hence, the accordion does not have the UI elements (children) yet
     * @param context
     */
    private fun prepareLayoutWithoutChildren(context: Context) {
        initializeViewWithoutChildren(context)

        headingLayout.layoutTransition = if (isAnimated) LayoutTransition() else null
        heading.text = headingString

        body.visibility = VISIBLE
        partition.visibility = if (isPartitioned) VISIBLE else INVISIBLE

        if (isExpanded) expand() else collapse()

        setOnClickListenerOnHeading()
    }

    override fun onFinishInflate() {
        prepareLayout(context)
        super.onFinishInflate()
    }

    /***
     * This function expands the accordion
     */
    private fun expand() {
        if (isAnimated) {
            val expandAnimation =
                AccordionTransitionAnimation(body, 300, AccordionTransitionAnimation.EXPAND)
            expandAnimation.height = getFullHeight(body)
            expandAnimation.endBottomMargin = bodyBottomMargin
            expandAnimation.endTopMargin = bodyTopMargin
            body.startAnimation(expandAnimation)
        } else body.visibility = VISIBLE

        partition.visibility = if (isPartitioned) VISIBLE else INVISIBLE
        dropupImage.visibility = VISIBLE
        dropdownImage.visibility = GONE
        if (!isNullOrBlank(expandCollapseListener)) expandCollapseListener!!.onExpanded(this)
    }

    /***
     * This function collapses the accordion
     */
    private fun collapse() {
        if (isAnimated) {
            val collapseAnimation =
                AccordionTransitionAnimation(body, 300, AccordionTransitionAnimation.COLLAPSE)
            body.startAnimation(collapseAnimation)
        } else body.visibility = GONE

        partition.visibility = INVISIBLE
        dropupImage.visibility = GONE
        dropdownImage.visibility = VISIBLE
        if (!isNullOrBlank(expandCollapseListener)) expandCollapseListener!!.onCollapsed(this)
    }

    private fun setOnClickListenerOnHeading() {
        heading.setOnClickListener(togglebodyVisiblity)
        dropdownImage.setOnClickListener(togglebodyVisiblity)
        dropupImage.setOnClickListener(togglebodyVisiblity)
    }

    private var togglebodyVisiblity = OnClickListener {
        if (body.visibility == VISIBLE) collapse()
        else expand()
    }

    /***
     * This function adds the view to the body
     * @param child
     */
    fun addViewToBody(child: View) {
        body.addView(child)
    }

    /***
     * Set the heading string of the accordion
     * @param headingString
     */
    fun setHeadingString(headingString: String) {
        heading.text = headingString
    }

    fun setIsAnimated(isAnimated: Boolean) {
        this.isAnimated = isAnimated
    }

    /***
     * Get the status whether the accordion is going to animate when expanding/collapsing
     * @return
     */
    fun getAnimated(): Boolean {
        return isAnimated
    }

    /***
     * Set whether the accordion will play an animation when expanding/collapsing
     * @param animated
     */
    fun setAnimated(animated: Boolean) {
        isAnimated = animated
    }

    /***
     * Tell the accordion what to do when expanded or collapsed.
     * @param listener
     */
    fun setOnExpandCollapseListener(listener: AccordionExpansionCollapseListener) {
        this.expandCollapseListener = listener
    }

    /***
     * This function sets a click listener on the heading image
     * @return
     */
    fun setOnHeadingImageClickListener(listener: OnClickListener) {
        headingImage.setOnClickListener(listener)
    }

    /***
     * This function sets a long click listener on the heading image
     * @return
     */
    fun setOnHeadingImageLongClickListener(listener: OnLongClickListener) {
        headingImage.setOnLongClickListener(listener)
    }

    /***
     * This function returns the body of the accordion
     * @return
     */
    fun getBody(): RelativeLayout {
        return body
    }

    fun getExpanded(): Boolean {
        return isExpanded
    }

    /***
     * Tell the accordion whether to expand or remain collapsed by default, when drawn
     * @param expanded
     */
    fun setExpanded(expanded: Boolean) {
        isExpanded = expanded
    }

    /***
     * The status of the partition line
     * @return
     */
    fun getPartitioned(): Boolean {
        return isPartitioned
    }

    /***
     * This function tells the accordion whether to make the partition visible or not
     * @param partitioned
     */
    fun setPartitioned(partitioned: Boolean) {
        isPartitioned = partitioned
        partition.visibility = if (isPartitioned) VISIBLE else INVISIBLE
    }

    /***
     * This function adds a background drawable to the heading.
     * @param drawable
     */
    fun setHeadingBackground(drawable: Drawable) {
        if (isNullOrBlank(headingLayout)) headingLayout = findViewById(R.id.heading_layout)
        headingLayout.background = drawable
    }

    /***
     * This function adds a background drawable to the heading.
     * @param resId
     */
    fun setHeadingBackground(resId: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, resId, null)
        if (isNullOrBlank(headingLayout)) headingLayout = findViewById(R.id.heading_layout)
        headingLayout.background = drawable
    }

    /***
     * Sets the color of the heading
     * @param color
     */
    fun setHeadingColor(color: Int) {
        heading.setTextColor(color)
    }

    /***
     * Sets the image of the heading as a drawable
     * @param drawable
     */
    fun setHeadingImage(drawable: Drawable) {
        headingImage.setImageDrawable(drawable)
    }

    /***
     * Set the image of the heading as a resource id
     * @param resId
     */
    fun setHeadingImage(resId: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, resId, null)
        headingImage.setImageDrawable(drawable)
    }

    /***
     * This function adds a background drawable to the body.
     * @param drawable
     */
    fun setBodyBackground(drawable: Drawable) {
        if (isNullOrBlank(body)) body = findViewById(R.id.body_layout)
        body.background = drawable
    }

    /***
     * This function adds a background drawable to the body. Works only for JellyBean and above
     * @param resId
     */
    fun setBodyBackground(resId: Int) {
        val drawable = ResourcesCompat.getDrawable(resources, resId, null)
        if (isNullOrBlank(body)) body = findViewById(R.id.body_layout)
        body.background = drawable
    }

    /***
     * This function adds a background color to the heading.
     * @param color
     */
    fun setHeadingBackgroundColor(color: Int) {
        if (isNullOrBlank(headingLayout)) headingLayout = findViewById(R.id.heading_layout)
        headingLayout.setBackgroundColor(color)
    }
    
    /***
     * This function adds a background color to the body.
     * @param color
     */
    fun setBodyBackgroundColor(color: Int) {
        if (isNullOrBlank(body)) body = findViewById(R.id.body_layout)
        body.setBackgroundColor(color)
    }
}