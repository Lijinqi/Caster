/*
 *   Copyright 2014 Oguz Bilgener
 */
package east.orientation.caster.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import east.orientation.caster.R;
import east.orientation.caster.view.anim.DefaultAnimationHandler;
import east.orientation.caster.view.anim.MenuAnimationHandler;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_PHONE;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
import static android.view.WindowManager.LayoutParams.TYPE_TOAST;

/**
 * Provides the main structure of the menu.
 */

public class FloatingActionMenu {

    /**
     * Reference to the view (usually a button) to trigger the menu to show
     */
    private View mainActionView;
    /**
     * The angle (in degrees, modulus 360) which the circular menu starts from
     */
    private float startAngle;
    /**
     * The angle (in degrees, modulus 360) which the circular menu ends at
     */
    private float endAngle;
    /**
     * Distance of menu items from mainActionView
     */
    private int radius;
    /**
     * List of menu items
     */
    private List<Item> subActionItems;
    /**
     * Reference to the preferred {@link MenuAnimationHandler} object
     */
    private MenuAnimationHandler animationHandler;
    /**
     * Reference to a listener that listens open/close actions
     */
    private MenuStateChangeListener stateChangeListener;
    /**
     * whether the openings and closings should be animated or not
     */
    private boolean animated;
    /**
     * whether the menu is currently open or not
     */
    private boolean open;
    /**
     * whether the menu is an overlay for all other activities
     */
    private boolean systemOverlay;
    /**
     * a simple layout to contain all the sub action views in the system overlay mode
     */
    private FrameLayout overlayContainer;

    private OrientationEventListener orientationListener;

    private ActionViewLongPressListener actionViewLongPressListener;

    //记录当前手指位置在屏幕上的横坐标值
    private float xInScreen;

    //记录当前手指位置在屏幕上的纵坐标值
    private float yInScreen;

    //记录手指按下时在屏幕上的横坐标的值
    private float xDownInScreen;

    //记录手指按下时在屏幕上的纵坐标的值
    private float yDownInScreen;

    //记录手指按下时在小悬浮窗的View上的横坐标的值
    private float xInView;

    //记录手指按下时在小悬浮窗的View上的纵坐标的值
    private float yInView;

    private Handler mHandler;

    /**
     * Constructor that takes the parameters collected using {@link Builder}
     *
     * @param mainActionView
     * @param startAngle
     * @param endAngle
     * @param radius
     * @param subActionItems
     * @param animationHandler
     * @param animated
     */
    public FloatingActionMenu(final View mainActionView,
                              float startAngle,
                              float endAngle,
                              int radius,
                              List<Item> subActionItems,
                              MenuAnimationHandler animationHandler,
                              final boolean animated,
                              MenuStateChangeListener stateChangeListener,
                              boolean systemOverlay,
                              ActionViewLongPressListener actionViewLongPressListener) {
        this.mainActionView = mainActionView;
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.radius = radius;
        this.subActionItems = subActionItems;
        this.animationHandler = animationHandler;
        this.animated = animated;
        this.systemOverlay = systemOverlay;
        this.actionViewLongPressListener = actionViewLongPressListener;
        // The menu is initially closed.
        this.open = false;

        this.stateChangeListener = stateChangeListener;

        mHandler = new Handler();

        // Listen click events on the main action view
        // In the future, touch and drag events could be listened to offer an alternative behaviour
        GestureDetector detector = new GestureDetector(this.mainActionView.getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {

                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                // 动画
                toggle(animated);
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                // 长按
                actionViewLongPressListener.onLongPressed(mainActionView);
            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

                return false;
            }
        });
        this.mainActionView.setClickable(true);
        this.mainActionView.setOnClickListener(new ActionViewClickListener());
        this.mainActionView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                        xInView = event.getX();
                        yInView = event.getY();
                        xDownInScreen = event.getRawX();
                        yDownInScreen = event.getRawY() - getStatusBarHeight();
                        xInScreen = event.getRawX();
                        yInScreen = event.getRawY() - getStatusBarHeight();
                        //
                        calculateMaxOpenArc();

                        //
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                actionViewLongPressListener.onLongPressed(mainActionView);
//                            }
//                        },1500);
                        break;
                    case MotionEvent.ACTION_MOVE:

                        xInScreen = event.getRawX();
                        yInScreen = event.getRawY() - getStatusBarHeight();

                        // 手指移动的时候更新小悬浮窗的位置
                        // todo 改变位置
                        if (!open)
                            updateViewPosition();
                        //
                        calculateMaxOpenArc();
                        //
                        //mHandler.removeCallbacksAndMessages(null);
                        break;
                    case MotionEvent.ACTION_UP:
                        // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
//                        if(Math.abs(xInScreen-xDownInScreen) < 2 && Math.abs(yInScreen-yDownInScreen) < 2){
//                            // 超短距离移动视为点击
//                            toggle(animated);
//                        }
                        //
                        //mHandler.removeCallbacksAndMessages(null);

                        break;
                    default:
                        break;
                }
                return detector.onTouchEvent(event);
            }
        });

        // Do not forget to set the menu as self to our customizable animation handler
        if (animationHandler != null) {
            animationHandler.setMenu(this);
        }

        if (systemOverlay) {
            overlayContainer = new FrameLayout(mainActionView.getContext());
        } else {
            overlayContainer = null; // beware NullPointerExceptions!
        }

        // Find items with undefined sizes
        for (final Item item : subActionItems) {
            if (item.width == 0 || item.height == 0) {
                if (systemOverlay) {
                    throw new RuntimeException("Sub action views cannot be added without " +
                            "definite width and height.");
                }
                // Figure out the size by temporarily adding it to the Activity content view hierarchy
                // and ask the size from the system
                addViewToCurrentContainer(item.view);
                // Make item view invisible, just in case
                item.view.setAlpha(0);
                // Wait for the right time
                item.view.post(new ItemViewQueueListener(item));
            }
        }

        if (systemOverlay) {
            orientationListener = new OrientationEventListener(mainActionView.getContext(), SensorManager.SENSOR_DELAY_UI) {
                private int lastState = -1;

                @SuppressLint("WrongConstant")
                public void onOrientationChanged(int orientation) {

                    Display display = getWindowManager().getDefaultDisplay();
                    if (display.getRotation() != lastState) {
                        lastState = display.getRotation();
                        //
                        if (isOpen()) {
                            close(false);
                        }
                    }
                }
            };
            orientationListener.enable();
        }
    }

    /**
     * 计算最大展开角度
     */
    private void calculateMaxOpenArc() {
        Point center = getActionViewCenter();
        float radius = getRadius();
        float itemWidth = subActionItems.get(0).width;
        float itemHeight = subActionItems.get(0).height;
        double arc;

        if (center.x >= radius + itemWidth / 2 && center.y >= radius + itemHeight / 2
                && getScreenSize().x - center.x >= radius + itemWidth / 2 && getScreenSize().y - center.y >= radius + itemHeight / 2) {
            // 在中间
            startAngle = 0;
            endAngle = 360;
        } else if (center.x < radius + itemWidth / 2 && center.y > radius + itemHeight / 2 && getScreenSize().y - center.y > radius + itemHeight / 2) {
            // 在左中
            arc = Math.acos((center.x - itemWidth / 2) / radius) / Math.PI * 180;
            startAngle = (float) (arc - 180);
            endAngle = (float) (180 - arc);
        } else if (getScreenSize().x - center.x < radius + itemWidth / 2 && center.y > radius + itemHeight / 2 && getScreenSize().y - center.y > radius + itemHeight / 2) {
            // 在右中
            arc = Math.acos((getScreenSize().x - center.x - itemWidth / 2) / radius) / Math.PI * 180;
            startAngle = (float) (arc);
            endAngle = (float) (360 - arc);
        } else if (center.y < radius + itemWidth / 2 && center.x > radius + itemWidth / 2 && getScreenSize().x - center.x > radius + itemWidth / 2) {
            // 在上中
            arc = Math.acos((center.y - itemHeight / 2) / radius) / Math.PI * 180;
            startAngle = (float) (arc - 90);
            endAngle = (float) (270 - arc);
        } else if (getScreenSize().y - center.y < radius + itemWidth / 2 && center.x > radius + itemWidth / 2 && getScreenSize().x - center.x > radius + itemWidth / 2) {
            // 在下中
            arc = Math.acos((getScreenSize().y - center.y - itemHeight / 2) / radius) / Math.PI * 180;
            startAngle = (float) (arc + 90);
            endAngle = (float) (450 - arc);
        } else if (center.x < radius + itemWidth / 2 && center.y < radius + itemWidth / 2) {
            // 在左上
            startAngle = (float) (Math.acos((center.y - itemHeight / 2) / radius) / Math.PI * 180 - 90);
            endAngle = (float) (180 - Math.acos((center.x - itemWidth / 2) / radius) / Math.PI * 180);
        } else if (getScreenSize().x - center.x < radius + itemWidth / 2 && center.y < radius + itemWidth / 2) {
            // 在右上
            startAngle = (float) (Math.acos((getScreenSize().x - center.x - itemWidth / 2) / radius) / Math.PI * 180);
            endAngle = (float) (270 - Math.acos((center.y - itemHeight / 2) / radius) / Math.PI * 180);
        } else if (center.x < radius + itemWidth / 2 && getScreenSize().y - center.y < radius + itemWidth / 2) {
            // 在左下
            startAngle = (float) (Math.acos((center.x - itemWidth / 2) / radius) / Math.PI * 180 - 180);
            endAngle = (float) (90 - Math.acos((getScreenSize().y - center.y - itemHeight / 2) / radius) / Math.PI * 180);
        } else if (getScreenSize().x - center.x < radius + itemWidth / 2 && getScreenSize().y - center.y < radius + itemWidth / 2) {
            // 在右下
            startAngle = (float) (Math.acos((getScreenSize().y - center.y - itemHeight / 2) / radius) / Math.PI * 180 + 90);
            endAngle = (float) (360 - Math.acos((getScreenSize().x - center.x - itemWidth / 2) / radius) / Math.PI * 180);
        }
    }

    /**
     * Simply opens the menu by doing necessary calculations.
     *
     * @param animated if true, this action is executed by the current {@link MenuAnimationHandler}
     */
    public void open(boolean animated) {

        // Get the center of the action view from the following function for efficiency
        // populate destination x,y coordinates of Items
        Point center = calculateItemPositions();

        WindowManager.LayoutParams overlayParams = null;

        if (systemOverlay) {
            // If this is a system overlay menu, use the overlay container and place it behind
            // the main action button so that all the views will be added into it.
            attachOverlayContainer();

            overlayParams = (WindowManager.LayoutParams) overlayContainer.getLayoutParams();
        }

        if (animated && animationHandler != null) {
            // If animations are enabled and we have a MenuAnimationHandler, let it do the heavy work
            if (animationHandler.isAnimating()) {
                // Do not proceed if there is an animation currently going on.
                return;
            }

            for (int i = 0; i < subActionItems.size(); i++) {
                // It is required that these Item views are not currently added to any parent
                // Because they are supposed to be added to the Activity content view,
                // just before the animation starts
                if (subActionItems.get(i).view.getParent() != null) {
                    throw new RuntimeException("All of the sub action items have to be independent from a parent.");
                }

                // Initially, place all items right at the center of the main action view
                // Because they are supposed to start animating from that point.
                final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(subActionItems.get(i).width, subActionItems.get(i).height, Gravity.TOP | Gravity.LEFT);

                if (systemOverlay) {
                    params.setMargins(center.x - overlayParams.x - subActionItems.get(i).width / 2, center.y - overlayParams.y - subActionItems.get(i).height / 2, 0, 0);
                } else {
                    params.setMargins(center.x - subActionItems.get(i).width / 2, center.y - subActionItems.get(i).height / 2, 0, 0);
                }
                addViewToCurrentContainer(subActionItems.get(i).view, params);
            }
            // Tell the current MenuAnimationHandler to animate from the center
            animationHandler.animateMenuOpening(center);
        } else {
            // If animations are disabled, just place each of the items to their calculated destination positions.
            for (int i = 0; i < subActionItems.size(); i++) {
                // This is currently done by giving them large margins

                final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(subActionItems.get(i).width, subActionItems.get(i).height, Gravity.TOP | Gravity.LEFT);
                if (systemOverlay) {
                    params.setMargins(subActionItems.get(i).x - overlayParams.x, subActionItems.get(i).y - overlayParams.y, 0, 0);
                    subActionItems.get(i).view.setLayoutParams(params);
                } else {
                    params.setMargins(subActionItems.get(i).x, subActionItems.get(i).y, 0, 0);
                    subActionItems.get(i).view.setLayoutParams(params);
                    // Because they are placed into the main content view of the Activity,
                    // which is itself a FrameLayout
                }
                addViewToCurrentContainer(subActionItems.get(i).view, params);
            }
        }
        // do not forget to specify that the menu is open.
        open = true;

        if (stateChangeListener != null) {
            stateChangeListener.onMenuOpened(this);
        }

    }

    /**
     * Closes the menu.
     *
     * @param animated if true, this action is executed by the current {@link MenuAnimationHandler}
     */
    public void close(boolean animated) {
        // If animations are enabled and we have a MenuAnimationHandler, let it do the heavy work
        if (animated && animationHandler != null) {
            if (animationHandler.isAnimating()) {
                // Do not proceed if there is an animation currently going on.
                return;
            }
            animationHandler.animateMenuClosing(getActionViewCenter());
        } else {
            // If animations are disabled, just detach each of the Item views from the Activity content view.
            for (int i = 0; i < subActionItems.size(); i++) {
                removeViewFromCurrentContainer(subActionItems.get(i).view);
            }
            detachOverlayContainer();
        }
        // do not forget to specify that the menu is now closed.
        open = false;

        if (stateChangeListener != null) {
            stateChangeListener.onMenuClosed(this);
        }
    }

    /**
     * Toggles the menu
     *
     * @param animated if true, the open/close action is executed by the current {@link MenuAnimationHandler}
     */
    public void toggle(boolean animated) {
        if (open) {
            close(animated);
        } else {
            open(animated);
        }
    }

    /**
     * @return whether the menu is open or not
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * @return whether the menu is a system overlay or not
     */
    public boolean isSystemOverlay() {
        return systemOverlay;
    }

    public FrameLayout getOverlayContainer() {
        return overlayContainer;
    }

    /**
     * Recalculates the positions of each sub action item on demand.
     */
    public void updateItemPositions() {
        // Only update if the menu is currently open
        if (!isOpen()) {
            return;
        }
        // recalculate x,y coordinates of Items
        calculateItemPositions();

        // Simply update layout params for each item
        for (int i = 0; i < subActionItems.size(); i++) {
            // This is currently done by giving them large margins
            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(subActionItems.get(i).width, subActionItems.get(i).height, Gravity.TOP | Gravity.LEFT);
            params.setMargins(subActionItems.get(i).x, subActionItems.get(i).y, 0, 0);
            subActionItems.get(i).view.setLayoutParams(params);
        }
    }

    /**
     * Gets the coordinates of the main action view
     * This method should only be called after the main layout of the Activity is drawn,
     * such as when a user clicks the action button.
     *
     * @return a Point containing x and y coordinates of the top left corner of action view
     */
    private Point getActionViewCoordinates() {
        int[] coords = new int[2];
        // This method returns a x and y values that can be larger than the dimensions of the device screen.
        mainActionView.getLocationOnScreen(coords);

        // So, we need to deduce the offsets.
        if (systemOverlay) {
            coords[1] -= getStatusBarHeight();
        } else {
            Rect activityFrame = new Rect();
            getActivityContentView().getWindowVisibleDisplayFrame(activityFrame);
            coords[0] -= (getScreenSize().x - getActivityContentView().getMeasuredWidth());
            coords[1] -= (activityFrame.height() + activityFrame.top - getActivityContentView().getMeasuredHeight());
        }
        return new Point(coords[0], coords[1]);
    }

    /**
     * Returns the center point of the main action view
     *
     * @return the action view center point
     */
    public Point getActionViewCenter() {
        Point point = getActionViewCoordinates();
        point.x += mainActionView.getMeasuredWidth() / 2;
        point.y += mainActionView.getMeasuredHeight() / 2;
        return point;
    }

    /**
     * Calculates the desired positions of all items.
     *
     * @return getActionViewCenter()
     */
    private Point calculateItemPositions() {
        // Create an arc that starts from startAngle and ends at endAngle
        // in an area that is as large as 4*radius^2
        final Point center = getActionViewCenter();
        RectF area = new RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius);

        Path orbit = new Path();
        orbit.addArc(area, startAngle, endAngle - startAngle);

        PathMeasure measure = new PathMeasure(orbit, false);

        // Prevent overlapping when it is a full circle
        int divisor;
        if (Math.abs(endAngle - startAngle) >= 360 || subActionItems.size() <= 1) {
            divisor = subActionItems.size();
        } else {
            divisor = subActionItems.size() - 1;
        }

        // Measure this path, in order to find points that have the same distance between each other
        for (int i = 0; i < subActionItems.size(); i++) {
            float[] coords = new float[]{0f, 0f};
            measure.getPosTan((i) * measure.getLength() / divisor, coords, null);
            // get the x and y values of these points and set them to each of sub action items.
            subActionItems.get(i).x = (int) coords[0] - subActionItems.get(i).width / 2;
            subActionItems.get(i).y = (int) coords[1] - subActionItems.get(i).height / 2;
        }
        return center;
    }

    /**
     * @return the specified raduis of the menu
     */
    public int getRadius() {
        return radius;
    }

    /**
     * @return a reference to the sub action items list
     */
    public List<Item> getSubActionItems() {
        return subActionItems;
    }

    /**
     * Finds and returns the main content view from the Activity context.
     *
     * @return the main content view
     */
    public View getActivityContentView() {
        try {
            return ((Activity) mainActionView.getContext()).getWindow().getDecorView().findViewById(android.R.id.content);
        } catch (ClassCastException e) {
            throw new ClassCastException("Please provide an Activity context for this FloatingActionMenu.");
        }
    }

    /**
     * Intended to use for systemOverlay mode.
     *
     * @return the WindowManager for the current context.
     */
    public WindowManager getWindowManager() {
        return (WindowManager) mainActionView.getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    private void addViewToCurrentContainer(View view, ViewGroup.LayoutParams layoutParams) {
        if (systemOverlay) {
            overlayContainer.addView(view, layoutParams);
        } else {
            try {
                if (layoutParams != null) {
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) layoutParams;
                    ((ViewGroup) getActivityContentView()).addView(view, lp);
                } else {
                    ((ViewGroup) getActivityContentView()).addView(view);
                }
            } catch (ClassCastException e) {
                throw new ClassCastException("layoutParams must be an instance of " +
                        "FrameLayout.LayoutParams.");
            }
        }
    }

    public void attachOverlayContainer() {
        try {
            WindowManager.LayoutParams overlayParams = calculateOverlayContainerParams();

            overlayContainer.setLayoutParams(overlayParams);
            if (overlayContainer.getParent() == null) {
                getWindowManager().addView(overlayContainer, overlayParams);
            }
            getWindowManager().updateViewLayout(mainActionView, mainActionView.getLayoutParams());
        } catch (SecurityException e) {
            throw new SecurityException("Your application must have SYSTEM_ALERT_WINDOW " +
                    "permission to create a system window.");
        }
    }

    private WindowManager.LayoutParams calculateOverlayContainerParams() {
        // calculate the minimum viable size of overlayContainer
        WindowManager.LayoutParams overlayParams = getDefaultSystemWindowParams();
        int left = 9999, right = 0, top = 9999, bottom = 0;
        for (int i = 0; i < subActionItems.size(); i++) {
            int lm = subActionItems.get(i).x;
            int tm = subActionItems.get(i).y;

            if (lm < left) {
                left = lm;
            }
            if (tm < top) {
                top = tm;
            }
            if (lm + subActionItems.get(i).width > right) {
                right = lm + subActionItems.get(i).width;
            }
            if (tm + subActionItems.get(i).height > bottom) {
                bottom = tm + subActionItems.get(i).height;
            }
        }
        overlayParams.width = right - left;
        overlayParams.height = bottom - top;
        overlayParams.x = left;
        overlayParams.y = top;

        overlayParams.gravity = Gravity.TOP | Gravity.LEFT;
        return overlayParams;
    }

    public void detachOverlayContainer() {
        getWindowManager().removeView(overlayContainer);
    }

    public int getStatusBarHeight() {
        int result = 0;
//        int resourceId = mainActionView.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            result = mainActionView.getContext().getResources().getDimensionPixelSize(resourceId);
//        }
        return result;
    }

    public void addViewToCurrentContainer(View view) {
        addViewToCurrentContainer(view, null);
    }

    public void removeViewFromCurrentContainer(View view) {
        if (systemOverlay) {
            overlayContainer.removeView(view);
        } else {
            ((ViewGroup) getActivityContentView()).removeView(view);
        }
    }

    /**
     * Retrieves the screen size from the Activity context
     *
     * @return the screen size as a Point object
     */
    private Point getScreenSize() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        return size;
    }

    public void setStateChangeListener(MenuStateChangeListener listener) {
        this.stateChangeListener = listener;
    }

    public void setActionViewLongPressesLisener(ActionViewLongPressListener actionViewLongPressesLisener) {
        this.actionViewLongPressListener = actionViewLongPressesLisener;
    }

    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        WindowManager.LayoutParams params = FloatingActionButton.Builder.getDefaultSystemWindowParams(mainActionView.getContext());
        params.x = (int) (xInScreen - xInView);
        params.y = (int) (yInScreen - yInView);
        getWindowManager().updateViewLayout(mainActionView, params);
    }

    /**
     * A simple click listener used by the main action view
     */
    public class ActionViewClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            toggle(animated);
        }
    }

    /**
     * This runnable calculates sizes of Item views that are added to the menu.
     */
    private class ItemViewQueueListener implements Runnable {

        private static final int MAX_TRIES = 10;
        private Item item;
        private int tries;

        public ItemViewQueueListener(Item item) {
            this.item = item;
            this.tries = 0;
        }

        @Override
        public void run() {
            // Wait until the the view can be measured but do not push too hard.
            if (item.view.getMeasuredWidth() == 0 && tries < MAX_TRIES) {
                item.view.post(this);
                return;
            }
            // Measure the size of the item view
            item.width = item.view.getMeasuredWidth();
            item.height = item.view.getMeasuredHeight();

            // Revert everything left_back to normal
            item.view.setAlpha(item.alpha);
            // Remove the item view from view hierarchy
            removeViewFromCurrentContainer(item.view);
        }
    }

    /**
     * A simple structure to put a view and its x, y, width and height values together
     */
    public static class Item {
        public int x;
        public int y;
        public int width;
        public int height;

        public float alpha;

        public View view;

        public Item(View view, int width, int height) {
            this.view = view;
            this.width = width;
            this.height = height;
            alpha = view.getAlpha();
            x = 0;
            y = 0;
        }
    }

    /**
     * A listener to listen open/closed state changes of the Menu
     */
    public static interface MenuStateChangeListener {
        public void onMenuOpened(FloatingActionMenu menu);

        public void onMenuClosed(FloatingActionMenu menu);
    }

    public static interface ActionViewLongPressListener {
        void onLongPressed(View actionView);
    }

    /**
     * A builder for {@link FloatingActionMenu} in conventional Java Builder format
     */
    public static class Builder {

        private float startAngle;
        private float endAngle;
        private int radius;
        private View actionView;
        private List<Item> subActionItems;
        private MenuAnimationHandler animationHandler;
        private boolean animated;
        private MenuStateChangeListener stateChangeListener;
        private boolean systemOverlay;
        private ActionViewLongPressListener actionViewLongPressListener;

        public Builder(Context context, boolean systemOverlay) {
            subActionItems = new ArrayList<Item>();
            // Default settings
            radius = context.getResources().getDimensionPixelSize(R.dimen.action_menu_radius);
            startAngle = 180;
            endAngle = 270;
            animationHandler = new DefaultAnimationHandler();
            animated = true;
            this.systemOverlay = systemOverlay;
        }

        public Builder(Context context) {
            this(context, false);
        }

        public Builder setStartAngle(float startAngle) {
            this.startAngle = startAngle;
            return this;
        }

        public Builder setEndAngle(float endAngle) {
            this.endAngle = endAngle;
            return this;
        }

        public Builder setRadius(int radius) {
            this.radius = radius;
            return this;
        }

        public Builder addSubActionView(View subActionView, int width, int height) {
            subActionItems.add(new Item(subActionView, width, height));
            return this;
        }

        /**
         * Adds a sub action view that is already alive, but not added to a parent View.
         *
         * @param subActionView a view for the menu
         * @return the builder object itself
         */
        public Builder addSubActionView(View subActionView) {
            if (systemOverlay) {
                throw new RuntimeException("Sub action views cannot be added without " +
                        "definite width and height. Please use " +
                        "other methods named addSubActionView");
            }
            return this.addSubActionView(subActionView, 0, 0);
        }

        /**
         * Inflates a new view from the specified resource id and adds it as a sub action view.
         *
         * @param resId   the resource id reference for the view
         * @param context a valid context
         * @return the builder object itself
         */
        public Builder addSubActionView(int resId, Context context) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(resId, null, false);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            return this.addSubActionView(view, view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        /**
         * Sets the current animation handler to the specified MenuAnimationHandler child
         *
         * @param animationHandler a MenuAnimationHandler child
         * @return the builder object itself
         */
        public Builder setAnimationHandler(MenuAnimationHandler animationHandler) {
            this.animationHandler = animationHandler;
            return this;
        }

        public Builder enableAnimations() {
            animated = true;
            return this;
        }

        public Builder disableAnimations() {
            animated = false;
            return this;
        }

        public Builder setStateChangeListener(MenuStateChangeListener listener) {
            stateChangeListener = listener;
            return this;
        }

        public Builder setActionViewLongPressListener(ActionViewLongPressListener listener) {
            actionViewLongPressListener = listener;
            return this;
        }

        public Builder setSystemOverlay(boolean systemOverlay) {
            this.systemOverlay = systemOverlay;
            return this;
        }

        /**
         * Attaches the whole menu around a main action view, usually a button.
         * All the calculations are made according to this action view.
         *
         * @param actionView
         * @return the builder object itself
         */
        public Builder attachTo(View actionView) {
            this.actionView = actionView;
            return this;
        }

        public FloatingActionMenu build() {
            return new FloatingActionMenu(actionView,
                    startAngle,
                    endAngle,
                    radius,
                    subActionItems,
                    animationHandler,
                    animated,
                    stateChangeListener,
                    systemOverlay,
                    actionViewLongPressListener);
        }
    }

    public static WindowManager.LayoutParams getDefaultSystemWindowParams() {
        int paramType = TYPE_PHONE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            paramType = TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            paramType = TYPE_PHONE;
        } else {
            paramType = TYPE_SYSTEM_ALERT;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR ,
                PixelFormat.TRANSLUCENT);
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        return params;
    }

}