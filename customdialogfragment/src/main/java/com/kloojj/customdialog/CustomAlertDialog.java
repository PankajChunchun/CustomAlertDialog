package com.kloojj.customdialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Pankaj Kumar on 08/08/18.
 * pankaj@kloojj.com
 * EAT | DRINK | CODE
 */
public class CustomAlertDialog extends DialogFragment {

    // Used to remove default padding of WebView.
    //Added color attribute to give background color as white for web view
    private static final String HTML_PREFIX = "<body style='margin:0;padding:0;background-color:#FFFFFF'>";
    private static final String HTML_POSTFIX = "</body>";
    // Constants for HTML/ WebView
    private static final String TEXT_HTML = "text/html";
    private static final String UTF_8 = "UTF-8";

    private static final int TITLE_TEXT_SIZE = 20;
    private static final int CONTENT_TEXT_SIZE = 14;

    // Common attributes
    private AppCompatActivity mActivity;
    private int mViewLayoutResId;
    private View mDialogParentView;
    private int mViewSpacingLeft;
    private int mViewSpacingTop;
    private int mViewSpacingRight;
    private int mViewSpacingBottom;
    private boolean mViewSpacingSpecified = false;

    // Title view attributes
    private int mIconId = 0;
    private Drawable mIcon;
    private CharSequence mTitle;
    private View mTitleContainerView;
    private TextView mAlertTitleView;
    private ImageView mAlertTitleIconView;

    // Content view attributes
    private CharSequence mMessage;
    private View mDefaultContentViewContainer;
    private View mCustomContentViewContainer;
    private View mCustomeContentView;
    private WebView mContentWebView;
    private FrameLayout mWebviewMessageFramelayout;
    private TextView mContentTextView;
    private boolean isWebviewMessageEnabled;
    private boolean isSimpleContentEnabled = false;

    // Bottom view/ Button attributes
    private AppCompatButton mButtonPositive;
    private CharSequence mButtonPositiveText;
    private View.OnClickListener mPositiveButtonListener;

    private AppCompatButton mButtonNegative;
    private CharSequence mButtonNegativeText;
    private View.OnClickListener mNegativeButtonListener;

    private AppCompatButton mButtonNeutral;
    private CharSequence mButtonNeutralText;
    private View.OnClickListener mNeutralButtonListener;

    private boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = true;
    private DialogInterface.OnCancelListener mOnCancelListener;
    private DialogInterface.OnShowListener mShownListener;
    private DialogInterface.OnDismissListener mOnDismissListener;
    private DialogInterface.OnKeyListener mOnKeyListener;
    private boolean isShowFullScreenDialog;
    private ImageView mCloseButton;
    private View.OnClickListener mCloseButtonListener;
    private final int BUTTON_ICON = -5;
    private NestedScrollView mScrollView, mCustomScroll;

    //Flag to maintain modal dismiss state
    private boolean shouldDismissModal = true;

    private CustomAlertDialog.OnBackPressedListener mOnBackPressedListener;

    private static void handleVisibilityOfScrollIndicators(View v, View upIndicator, View downIndicator) {
        if (upIndicator != null) {
            upIndicator.setVisibility(
                    ViewCompat.canScrollVertically(v, -1) ? View.VISIBLE : View.INVISIBLE);
        }
        if (downIndicator != null) {
            downIndicator.setVisibility(
                    ViewCompat.canScrollVertically(v, 1) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Handle orientation change.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mDialogParentView = inflater.inflate(R.layout.custom_dialog_layout, container, false);

        initDialogView();
        setupDialogView();

        return mDialogParentView;
    }

    @Override
    public void onDestroyView() {
        // There is an open defect (https://code.google.com/p/android/issues/detail?id=17423) into compatibility library
        // which makes dialog disappear if setRetainInstance(true) has been used to DialogFragment.

        // Below is fix for that.
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null); // getDialog().setOnDismissListener(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isShowFullScreenDialog) {
            setUpDialogSize();
        }
    }

    private void setUpDialogSize() {
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(dm);
        FrameLayout.LayoutParams dialogFragmentLayoutParams = (FrameLayout.LayoutParams) getView().getLayoutParams();
        // Set fixed width for tablet
        if (Utils.isRunningOnTablet(getContext())) {
            params.width = dm.widthPixels;
            dialogFragmentLayoutParams.width =
                    (int) getResources().getDimension(R.dimen.tablet_fixed_dialog_extra_width) * 6;
            dialogFragmentLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        } else {
            int width = dm.widthPixels;
            params.width = width;
        }

        int left = (int) getResources().getDimension(R.dimen.dialog_margin_left);
        int right = (int) getResources().getDimension(R.dimen.dialog_margin_right);
        int top = (int) getResources().getDimension(R.dimen.dialog_margin_top);
        int bottom = (int) getResources().getDimension(R.dimen.dialog_margin_bottom);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            top = (int) getResources().getDimension(R.dimen.dialog_margin_top_above_L);
        }
        dialogFragmentLayoutParams.setMargins(left, top, right, bottom);

        getView().setLayoutParams(dialogFragmentLayoutParams);
        getView().setBackgroundResource((R.drawable.custom_dialog_background));
        getDialog().getWindow().setDimAmount(.6f);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        window.setAttributes(params);
        window.setGravity(Gravity.CENTER);
    }

    /**
     * Initialise dialog attributes.
     */
    private void initDialogView() {
        // Init title views
        mAlertTitleView = (TextView) mDialogParentView.findViewById(R.id.alertTitle);
        mAlertTitleIconView = (ImageView) mDialogParentView.findViewById(R.id.alertIcon);
        mTitleContainerView = mDialogParentView.findViewById(R.id.title_template);
        // Init content views
        mContentTextView = (TextView) mDialogParentView.findViewById(R.id.message);
        mContentWebView = (WebView) mDialogParentView.findViewById(R.id.webviewMessage);
        mWebviewMessageFramelayout = (FrameLayout) mDialogParentView.findViewById(R.id.webviewMessageFramelayout);
        mDefaultContentViewContainer = mDialogParentView.findViewById(R.id.contentPanel);
        mCustomContentViewContainer = mDialogParentView.findViewById(R.id.custom);
        // Init bottom views
        mButtonPositive = (AppCompatButton) mDialogParentView.findViewById(R.id.buttonPositive);
        mButtonNegative = (AppCompatButton) mDialogParentView.findViewById(R.id.buttonNegative);
        mButtonNeutral = (AppCompatButton) mDialogParentView.findViewById(R.id.buttonNeutral);
        mCloseButton = (ImageView) mDialogParentView.findViewById(R.id.alertIcon);
    }

    /**
     * Setup view depending on input from caller. It will hide views other than configured views.
     */
    private void setupDialogView() {
        final View parentPanel = mDialogParentView.findViewById(R.id.parentPanel);
        final View defaultTopPanel = parentPanel.findViewById(R.id.topPanel);
        final View defaultContentPanel = parentPanel.findViewById(R.id.contentPanel);
        final View defaultButtonPanel = parentPanel.findViewById(R.id.button_layout);

        // Handle custom content before setting up the title or buttons so
        // that we can handle panel overrides.
        final ViewGroup customPanel = (ViewGroup) parentPanel.findViewById(R.id.customPanel);
        setupCustomContent(customPanel);

        final View customTopPanel = customPanel.findViewById(R.id.topPanel);
        final View customContentPanel = customPanel.findViewById(R.id.contentPanel);
        final View customButtonPanel = customPanel.findViewById(R.id.button_layout);

        // Resolve the correct panels and remove the defaults, if needed.
        final ViewGroup topPanel = resolvePanel(customTopPanel, defaultTopPanel);
        final ViewGroup contentPanel = resolvePanel(customContentPanel, defaultContentPanel);
        final ViewGroup buttonPanel = resolvePanel(customButtonPanel, defaultButtonPanel);

        setupDialogContent(contentPanel);
        setupDialogButtons(buttonPanel);
        setupDialogTitle(topPanel);

        final boolean hasCustomPanel = customPanel != null && customPanel.getVisibility() != View.GONE;
        final boolean hasTopPanel = topPanel != null && topPanel.getVisibility() != View.GONE;
        final boolean hasButtonPanel = buttonPanel != null && buttonPanel.getVisibility() != View.GONE;

        // Only display the text spacer if we don't have buttons.
        if (!hasButtonPanel) {
            if (contentPanel != null) {
                final View spacer = contentPanel.findViewById(R.id.textSpacerNoButtons);
                if (spacer != null) {
                    spacer.setVisibility(View.VISIBLE);
                }
            }
        }

        if (hasTopPanel) {
            // Only clip scrolling content to padding if we have a title.
            if (mScrollView != null) {
                mScrollView.setClipToPadding(true);
            } else if (mCustomScroll != null) {
                mCustomScroll.setClipToPadding(true);
            }
        }

        setFullScreenDialogParams(parentPanel, defaultTopPanel);

        // Update scroll indicators as needed.
        if (!hasCustomPanel) {
            final View content = mScrollView;
            if (content != null) {
                final int indicators = (hasTopPanel ? ViewCompat.SCROLL_INDICATOR_TOP : 0)
                        | (hasButtonPanel ? ViewCompat.SCROLL_INDICATOR_BOTTOM : 0);
                setScrollIndicators(contentPanel, content, indicators,
                        ViewCompat.SCROLL_INDICATOR_TOP | ViewCompat.SCROLL_INDICATOR_BOTTOM);
            }
        } else {
            final View content = mCustomScroll;
            if (content != null) {
                final int indicators = (hasTopPanel ? ViewCompat.SCROLL_INDICATOR_TOP : 0)
                        | (hasButtonPanel ? ViewCompat.SCROLL_INDICATOR_BOTTOM : 0);
                setScrollIndicators(contentPanel, content, indicators,
                        ViewCompat.SCROLL_INDICATOR_TOP | ViewCompat.SCROLL_INDICATOR_BOTTOM);
            }
        }

        setupListeners();
    }

    /**
     * Full screen modal has different UI specifications than other modals
     * So below method is to apply specifications for fullscreen modal.
     */
    private void setFullScreenDialogParams(View parentPanel, View defaultTopPanel) {

        if (isShowFullScreenDialog) {
            Resources r = getContext().getResources();
            DisplayMetrics dm = r.getDisplayMetrics();
            int dialog_margins = (int) getResources().getDimension(R.dimen.full_screen_dialog_margins);
            int top_margin = (int) getResources().getDimension(R.dimen.full_screen_dialog_margin_top);

            /**To remove any extra paddings and margins applied through xml for other Modals*/
            LinearLayout.LayoutParams lPTopPanel = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            /** Calculate ActionBar height*/
            int actionBarSize = getThemeAttributeDimensionSize(getContext(), android.R.attr.actionBarSize);

            int defaultMargin = (int) getResources().getDimension(R.dimen.default_margin);
            lPTopPanel.setMargins(defaultMargin, defaultMargin, defaultMargin, defaultMargin);

            /**Setting minimum height to adjust height according to title text length */
            defaultTopPanel.setMinimumHeight(actionBarSize);
            defaultTopPanel.setLayoutParams(lPTopPanel);

            /**Adding shadow below header using elevation */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                defaultTopPanel.setBackgroundColor(Color.WHITE);
                defaultTopPanel.setElevation((float) 4);
            }

            /**To set margins for Content View*/
            LinearLayout.LayoutParams lPContentView = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lPContentView.setMargins(dialog_margins, dialog_margins, dialog_margins,
                    dialog_margins); // llp_contentView.setMargins(left, top, right, bottom);
            mDefaultContentViewContainer.setLayoutParams(lPContentView);

            /**To Remove Margins for Content text applied through xml for other modal */
            LinearLayout.LayoutParams lPContentText = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lPContentText.setMargins(defaultMargin, defaultMargin, defaultMargin, defaultMargin);
            mContentTextView.setLayoutParams(lPContentText);
            mContentTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, CONTENT_TEXT_SIZE);

            /**To set Margins for Title text*/
            mAlertTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TITLE_TEXT_SIZE);
            LinearLayout.LayoutParams lPTitle = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lPTitle.setMargins(dialog_margins, defaultMargin, dialog_margins, defaultMargin);
            mAlertTitleView.setLayoutParams(lPTitle);

            /**To set Margins for Header Container*/
            LinearLayout.LayoutParams lPTitleContainer = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lPTitleContainer.setMargins(dialog_margins, top_margin, defaultMargin, defaultMargin);
            mTitleContainerView.setLayoutParams(lPTitleContainer);

            /** Set margin top to avoid issue - above lollipop version Header hides behind Status Bar*/
            int top = defaultMargin;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                top = (int) getResources().getDimension(R.dimen.dialog_margin_top);
            }

            parentPanel.setPadding(defaultMargin, top, defaultMargin, defaultMargin);
        }
    }

    /**
     * Method to get height of Action bar attribute for Full Screen Dialog Header
     */
    private int getThemeAttributeDimensionSize(Context context, int attr) {
        TypedArray a = null;
        try {
            a = context.getTheme().obtainStyledAttributes(new int[]{attr});
            return a.getDimensionPixelSize(0, 0);
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }

    private void setScrollIndicators(ViewGroup contentPanel, View content,
            final int indicators, final int mask) {
        // Set up scroll indicators (if present).
        View indicatorUp = mDialogParentView.findViewById(R.id.scrollIndicatorUp);
        View indicatorDown = mDialogParentView.findViewById(R.id.scrollIndicatorDown);
        View customIndicatorUp = mDialogParentView.findViewById(R.id.customScrollIndicatorUp);
        View customIndicatorDown = mDialogParentView.findViewById(R.id.customScrollIndicatorDown);

        if (Build.VERSION.SDK_INT >= 23) {
            // We're on Marshmallow so can rely on the View APIs
            ViewCompat.setScrollIndicators(content, indicators, mask);
            // We can also remove the compat indicator views
            if (indicatorUp != null || customIndicatorUp != null) {
                contentPanel.removeView(indicatorUp);
                contentPanel.removeView(customIndicatorUp);
            }
            if (indicatorDown != null || customIndicatorDown != null) {
                contentPanel.removeView(indicatorDown);
                contentPanel.removeView(customIndicatorDown);
            }
        } else {
            // First, remove the indicator views if we're not set to use them
            if (indicatorUp != null && (indicators & ViewCompat.SCROLL_INDICATOR_TOP) == 0
                    || customIndicatorUp != null && (indicators & ViewCompat.SCROLL_INDICATOR_TOP) == 0) {
                contentPanel.removeView(indicatorUp);
                indicatorUp = null;
                contentPanel.removeView(customIndicatorUp);
                customIndicatorUp = null;
            }
            if (indicatorDown != null && (indicators & ViewCompat.SCROLL_INDICATOR_BOTTOM) == 0
                    || customIndicatorDown != null && (indicators & ViewCompat.SCROLL_INDICATOR_BOTTOM) == 0) {
                contentPanel.removeView(indicatorDown);
                indicatorDown = null;
                contentPanel.removeView(customIndicatorDown);
                customIndicatorDown = null;
            }

            if (indicatorUp != null || indicatorDown != null || customIndicatorUp != null
                    || customIndicatorDown != null) {
                final View top = indicatorUp;
                final View bottom = indicatorDown;
                final View customTop = customIndicatorUp;
                final View customBottom = customIndicatorDown;

                if (mMessage != null) {
                    // We're just showing the ScrollView, set up listener.
                    mScrollView.setOnScrollChangeListener(
                            new NestedScrollView.OnScrollChangeListener() {
                                @Override
                                public void onScrollChange(NestedScrollView v, int scrollX,
                                        int scrollY,
                                        int oldScrollX, int oldScrollY) {
                                    handleVisibilityOfScrollIndicators(v, top, bottom);
                                }
                            });
                    // Set up the indicators following layout.
                    mScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            handleVisibilityOfScrollIndicators(mScrollView, top, bottom);
                        }
                    });
                } else {
                    //fot customView
                    mCustomScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(NestedScrollView nestedScrollView, int i, int i1, int i2, int i3) {
                            handleVisibilityOfScrollIndicators(nestedScrollView, null, customBottom);
                        }
                    });
                    mCustomScroll.post(new Runnable() {
                        @Override
                        public void run() {
                            handleVisibilityOfScrollIndicators(mCustomScroll, null, customBottom);
                        }
                    });
                    // We don't have any content to scroll, remove the indicators.
                    if (top != null) {
                        contentPanel.removeView(top);
                    }
                    if (bottom != null) {
                        contentPanel.removeView(bottom);
                    }
                }
            }
        }
    }

    private void setupCustomContent(ViewGroup customPanel) {
        mCustomScroll = (NestedScrollView) customPanel.findViewById(R.id.customScrollView);
        mCustomScroll.setFocusable(false);
        mCustomScroll.setNestedScrollingEnabled(false);
        final View customView;
        if (mCustomeContentView != null) {
            customView = mCustomeContentView;
        } else if (mViewLayoutResId != 0) {
            final LayoutInflater inflater = LayoutInflater.from(mActivity);
            customView = inflater.inflate(mViewLayoutResId, customPanel, false);
        } else {
            customView = null;
        }

        final boolean hasCustomView = customView != null;

        if (hasCustomView) {
            final FrameLayout custom = (FrameLayout) mDialogParentView.findViewById(R.id.custom);
            ViewGroup parent = ((ViewGroup) customView.getParent());
            if (null != parent) {
                parent.removeView(customView);
            }
            custom.addView(customView,
                    new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            if (mViewSpacingSpecified) {
                custom.setPadding(
                        mViewSpacingLeft, mViewSpacingTop, mViewSpacingRight, mViewSpacingBottom);
            }

        } else {
            customPanel.setVisibility(View.GONE);
        }
    }

    private void setupDialogTitle(ViewGroup topPanel) {
        mTitleContainerView = topPanel.findViewById(R.id.title_template);
        mAlertTitleView = (TextView) topPanel.findViewById(R.id.alertTitle);
        mAlertTitleIconView = (ImageView) topPanel.findViewById(R.id.alertIcon);

        final boolean hasTextTitle = !TextUtils.isEmpty(mTitle);
        if (hasTextTitle) {
            // Display the title if a title is supplied, else hide it.
            mAlertTitleView.setText(mTitle);

            // Do this last so that if the user has supplied any icons we
            // use them instead of the default ones. If the user has
            // specified 0 then make it disappear.
            if (mIconId != 0) {
                mAlertTitleIconView.setImageResource(mIconId);
            } else if (mIcon != null) {
                mAlertTitleIconView.setImageDrawable(mIcon);
            } else {
                // Apply the padding from the icon to ensure the title is
                // aligned correctly.
                mAlertTitleView.setPadding(mAlertTitleIconView.getPaddingLeft(),
                        mAlertTitleIconView.getPaddingTop(),
                        mAlertTitleIconView.getPaddingRight(),
                        mAlertTitleIconView.getPaddingBottom());
                mAlertTitleIconView.setVisibility(View.GONE);
            }
        } else {
            // Hide the title template
            mTitleContainerView.setVisibility(View.GONE);
            mAlertTitleIconView.setVisibility(View.GONE);
            topPanel.setVisibility(View.GONE);
        }
    }

    private void setupDialogContent(ViewGroup contentPanel) {
        mScrollView = (NestedScrollView) contentPanel.findViewById(R.id.scrollView);
        mScrollView.setFocusable(false);
        mScrollView.setNestedScrollingEnabled(false);

        // Special case for users that only want to display a String
        mContentTextView = (TextView) contentPanel.findViewById(R.id.message);
        mContentWebView = (WebView) mDialogParentView.findViewById(R.id.webviewMessage);

        if ((mContentTextView == null) && (mContentWebView == null)) {
            return;
        }

        if (mMessage != null) {

            // Check if incoming message is intended to show into webview.
            if (isWebviewMessageEnabled) {
                mContentTextView.setVisibility(View.GONE);
                mContentWebView.setVisibility(View.VISIBLE);
                // Set transparent color to match dialog content background color. Using below code also fixes the flicker issue in webview
                // while scrolling it. Note that WebView.setBackgroundColor(Color.TRANSPARENT) can also set transparent background but does
                // not solve the flicker issue.
                mContentWebView.setBackgroundColor(Color.argb(1, 0, 0, 0));
                mContentWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                        handler.proceed(); // Ignore SSL certificate errors
                    }
                });
                mContentWebView
                        .loadDataWithBaseURL(null, HTML_PREFIX + mMessage + HTML_POSTFIX, TEXT_HTML, UTF_8, null);
            } else {
                mContentWebView.setVisibility(View.GONE);
                mWebviewMessageFramelayout.setVisibility(View.GONE);
                mContentTextView.setVisibility(View.VISIBLE);
                if (!isSimpleContentEnabled) {
                    mContentTextView.setText(Html.fromHtml(mMessage.toString()));
                    // Add code for linkyfy to urls or phone number
                } else {
                    mContentTextView.setText(mMessage.toString());
                }
            }
        } else {
            mContentTextView.setVisibility(View.GONE);
            mContentWebView.setVisibility(View.GONE);
            mScrollView.removeView(mContentTextView);
            mDefaultContentViewContainer.setVisibility(View.GONE);
        }
    }

    private void setupDialogButtons(ViewGroup buttonPanel) {
        int BIT_BUTTON_POSITIVE = 1;
        int BIT_BUTTON_NEGATIVE = 2;
        int BIT_BUTTON_NEUTRAL = 4;
        int whichButtons = 0;

        mButtonPositive = (AppCompatButton) buttonPanel.findViewById(R.id.buttonPositive);
        mButtonPositive.setOnClickListener(new DefaultClickListener(DialogInterface.BUTTON_POSITIVE));

        if (TextUtils.isEmpty(mButtonPositiveText)) {
            mButtonPositive.setVisibility(View.GONE);
        } else {
            mButtonPositive.setText(mButtonPositiveText);
            mButtonPositive.setVisibility(View.VISIBLE);
            whichButtons = whichButtons | BIT_BUTTON_POSITIVE;
        }

        mButtonNegative = (AppCompatButton) buttonPanel.findViewById(R.id.buttonNegative);
        mButtonNegative.setOnClickListener(new DefaultClickListener(DialogInterface.BUTTON_NEGATIVE));

        if (TextUtils.isEmpty(mButtonNegativeText)) {
            mButtonNegative.setVisibility(View.GONE);
        } else {
            mButtonNegative.setText(mButtonNegativeText);
            mButtonNegative.setVisibility(View.VISIBLE);

            whichButtons = whichButtons | BIT_BUTTON_NEGATIVE;
        }

        mButtonNeutral = (AppCompatButton) buttonPanel.findViewById(R.id.buttonNeutral);
        mButtonNeutral.setOnClickListener(new DefaultClickListener(DialogInterface.BUTTON_NEUTRAL));

        if (TextUtils.isEmpty(mButtonNeutralText)) {
            mButtonNeutral.setVisibility(View.GONE);
        } else {
            mButtonNeutral.setText(mButtonNeutralText);
            mButtonNeutral.setVisibility(View.VISIBLE);

            whichButtons = whichButtons | BIT_BUTTON_NEUTRAL;
        }

        final boolean hasButtons = whichButtons != 0;
        if (!hasButtons) {
            buttonPanel.setVisibility(View.GONE);
        }
        if (isShowFullScreenDialog) {
            mCloseButton.setOnClickListener(new DefaultClickListener(BUTTON_ICON));
        }
    }

    private void setupListeners() {
        setCancelable(mCancelable);
        getDialog().setCanceledOnTouchOutside(mCanceledOnTouchOutside);
        getDialog().setOnShowListener(mShownListener);
        getDialog().setOnCancelListener(mOnCancelListener);
        getDialog().setOnDismissListener(mOnDismissListener);
        getDialog().setOnKeyListener(mOnKeyListener);
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    if (mOnBackPressedListener != null) {
                        mOnBackPressedListener.onBackPressedListener();
                        return false;
                    }
                }
                return false;
            }
        });
    }


    /**
     * Resolves whether a custom or default panel should be used. Removes the
     * default panel if a custom panel should be used. If the resolved panel is
     * a view stub, inflates before returning.
     *
     * @param customPanel  the custom panel
     * @param defaultPanel the default panel
     * @return the panel to use
     */
    private ViewGroup resolvePanel(View customPanel, View defaultPanel) {
        if (customPanel == null) {
            // Inflate the default panel, if needed.
            if (defaultPanel instanceof ViewStub) {
                defaultPanel = ((ViewStub) defaultPanel).inflate();
            }

            return (ViewGroup) defaultPanel;
        }

        // Remove the default panel entirely.
        if (defaultPanel != null) {
            final ViewParent parent = defaultPanel.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(defaultPanel);
            }
        }

        // Inflate the custom panel, if needed.
        if (customPanel instanceof ViewStub) {
            customPanel = ((ViewStub) customPanel).inflate();
        }

        return (ViewGroup) customPanel;
    }

    /**
     * Set Title for dialog.
     */
    public CustomAlertDialog setTitle(CharSequence title) {
        mTitle = title;
        if (mAlertTitleView != null) {
            mAlertTitleView.setText(title);
        }
        return this;
    }

    /**
     * Set message for dialog. Donot call setWebViewMessage() with this method call, in this case
     * order
     * will matter and last called method will get priority.
     */
    public CustomAlertDialog setMessage(CharSequence message) {
        isWebviewMessageEnabled = false;
        mMessage = message;
        if (mContentTextView != null) {
            mContentTextView.setText(message);
        }
        return this;
    }

    /**
     * Method to set Content Description for the message
     */
    public CustomAlertDialog setContentDesc(CharSequence message) {
        isWebviewMessageEnabled = false;
        if (mContentTextView != null) {
            mContentTextView.setContentDescription(message);
        }
        return this;
    }

    /**
     * flag to enable simple content
     */
    public void setSimpleContentMessage(boolean value) {
        isSimpleContentEnabled = value;

    }

    /**
     * Set message for dialog. Donot call setMessage() with this method call, in this case order
     * will matter and last called method will get priority.
     */
    public CustomAlertDialog setWebViewMessage(CharSequence message) {
        isWebviewMessageEnabled = true;
        mMessage = message;
        if (mContentWebView != null) {
            mContentWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed(); // Ignore SSL certificate errors
                }
            });
            mContentWebView.loadDataWithBaseURL(null, HTML_PREFIX + mMessage + HTML_POSTFIX, TEXT_HTML, UTF_8, null);
        }
        return this;
    }

    public View getCustomeContentView() {

        if (null != mCustomeContentView) {
            return mCustomeContentView;
        } else {
            return null;
        }
    }

    /**
     * Set the cutom view to display in the dialog.
     */
    public CustomAlertDialog setCustomContentView(View view) {
        return setCustomContentView(view, false, 0, 0, 0, 0);
    }

    /**
     * Set the cutom view to display in the dialog along with the spacing around that view
     */
    public CustomAlertDialog setCustomContentView(View view, boolean mViewSpacingSpecified, int viewSpacingLeft,
            int viewSpacingTop, int viewSpacingRight,
            int viewSpacingBottom) {
        mCustomeContentView = view;
        mViewLayoutResId = 0;
        this.mViewSpacingSpecified = mViewSpacingSpecified;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingTop = viewSpacingTop;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingBottom = viewSpacingBottom;
        return this;
    }

    /**
     * Set the custom view resource to display in the dialog. without any padding to the view
     */
    public CustomAlertDialog setCustomContentView(int layoutResId) {
        return setCustomContentView(layoutResId, false, 0, 0, 0, 0);
    }

    /**
     * Set the custom view resource to display in the dialog. with user defined padding to the
     * layout.
     */
    public CustomAlertDialog setCustomContentView(int layoutResId, boolean mViewSpacingSpecified, int viewSpacingLeft,
            int viewSpacingTop, int viewSpacingRight,
            int viewSpacingBottom) {
        mCustomeContentView = null;
        mViewLayoutResId = layoutResId;
        this.mViewSpacingSpecified = mViewSpacingSpecified;
        mViewSpacingLeft = viewSpacingLeft;
        mViewSpacingTop = viewSpacingTop;
        mViewSpacingRight = viewSpacingRight;
        mViewSpacingBottom = viewSpacingBottom;
        return this;
    }

    /**
     * Specifies the icon to display next to the alert title.
     *
     * @param resId the resource identifier of the drawable to use as the icon,
     *              or 0 for no icon
     */
    public CustomAlertDialog setIcon(int resId) {
        mIcon = null;
        mIconId = resId;

        if (mAlertTitleIconView != null) {
            if (resId != 0) {
                mAlertTitleIconView.setVisibility(View.VISIBLE);
                mAlertTitleIconView.setImageResource(mIconId);
            } else {
                mAlertTitleIconView.setVisibility(View.GONE);
            }
        }
        return this;
    }

    /**
     * Specifies the icon to display next to the alert title.
     *
     * @param icon the drawable to use as the icon or null for no icon
     */
    public void setIcon(Drawable icon) {
        mIcon = icon;
        mIconId = 0;

        if (mAlertTitleIconView != null) {
            if (icon != null) {
                mAlertTitleIconView.setVisibility(View.VISIBLE);
                mAlertTitleIconView.setImageDrawable(icon);
            } else {
                mAlertTitleIconView.setVisibility(View.GONE);
            }
        }
    }

    public CustomAlertDialog setIconClick(final View.OnClickListener listener) {
        mCloseButtonListener = listener;
        return this;
    }

    /**
     * Get dialog button which is being shown.
     *
     * @return CmnButton
     */
    public AppCompatButton getButton(int whichButton) {
        switch (whichButton) {
            case DialogInterface.BUTTON_POSITIVE:
                return mButtonPositive;
            case DialogInterface.BUTTON_NEGATIVE:
                return mButtonNegative;
            case DialogInterface.BUTTON_NEUTRAL:
                return mButtonNeutral;
            default:
                return null;
        }
    }

    /**
     * Set DialogInterface.OnCancelListener for Dialog cancel.
     */
    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.mOnCancelListener = onCancelListener;
        if (getDialog() != null) {
            getDialog().setOnCancelListener(onCancelListener);
        }
    }

    /**
     * Set DialogInterface.OnDismissListener for Dialog dismiss callback.
     */
    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
        if (getDialog() != null) {
            getDialog().setOnDismissListener(onDismissListener);
        }
    }

    public void setOnShownListener(DialogInterface.OnShowListener mShownListener) {
        this.mShownListener = mShownListener;
        if (getDialog() != null) {
            getDialog().setOnShowListener(mShownListener);
        }
    }

    /**
     * Set DialogInterface.OnKeyListener for Dialog onKey listener.
     */
    public void setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
        this.mOnKeyListener = onKeyListener;
        if (getDialog() != null) {
            getDialog().setOnKeyListener(onKeyListener);
        }
    }

    /**
     * Set text and listener for positive button of modal.
     */
    public CustomAlertDialog setPositiveButton(CharSequence text, final View.OnClickListener listener) {
        mButtonPositiveText = text;
        mPositiveButtonListener = listener;

        return this;
    }

    /**
     * Set text and listener for negative button of modal.
     */
    public CustomAlertDialog setNegativeButton(CharSequence text, final View.OnClickListener listener) {
        mButtonNegativeText = text;
        mNegativeButtonListener = listener;

        return this;
    }

    /**
     * Set text and listener for neutral button of modal.
     */
    public CustomAlertDialog setNeutralButton(CharSequence text, final View.OnClickListener listener) {

        mButtonNeutralText = text;
        mNeutralButtonListener = listener;
        return this;
    }

    /**
     * Get View of dialog, which can be used to find some views.
     */
    public View getDialogParentView() {
        return mDialogParentView;
    }

    /**
     * Set dialog can be cancalable on back press, outside touch or not. This is same as
     * DialogFragment#setCancelable().
     */
    public CustomAlertDialog setCanCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        mCancelable = cancelable;
        return this;
    }

    /**
     * Set if dialog can be cancelable while touching outside of modal.
     */
    public CustomAlertDialog setCanceledOnTouchOutside(boolean cancelable) {
        mCanceledOnTouchOutside = cancelable;
        return this;
    }

    /**
     * Show dialog.
     */
    public CustomAlertDialog show(AppCompatActivity activity) {
        mActivity = activity;
        if (mActivity != null && !mActivity.isFinishing() && !isAdded()) {
            FragmentManager fm = mActivity.getSupportFragmentManager();
            CustomAlertDialog.this.show(fm, CustomAlertDialog.class.getSimpleName());
        }
        return this;
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    /**
     * Method to set full screen modal
     *
     * @return CustomAlertDialog
     */
    public CustomAlertDialog setFullscreenModal(boolean isShowFullScreen) {
        isShowFullScreenDialog = isShowFullScreen;
        setStyle(DialogFragment.STYLE_NORMAL, DialogFragment.STYLE_NO_TITLE);
        return this;
    }

    /**
     * Set OnBackPressedListener to handle back button functionality.
     */
    public CustomAlertDialog setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        mOnBackPressedListener = onBackPressedListener;
        return this;
    }

    /*
    This interface is used to handle back button functionality of the device.
     */
    public interface OnBackPressedListener {

        void onBackPressedListener();
    }

    /**
     * Default View.OnClickListener for Positive, Negative and Neutral button.
     */
    private class DefaultClickListener implements View.OnClickListener {

        private final int whichBtn;

        DefaultClickListener(int whichButton) {
            whichBtn = whichButton;
        }

        @Override
        public void onClick(View v) {
            Dialog dialog = getDialog();
            if (dialog != null && shouldDismissModal) {
                dialog.dismiss();
            }
            switch (whichBtn) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (mPositiveButtonListener != null) {
                        mPositiveButtonListener.onClick(v);
                    }
                    return;
                case DialogInterface.BUTTON_NEGATIVE:
                    if (mNegativeButtonListener != null) {
                        mNegativeButtonListener.onClick(v);
                    }
                    return;
                case DialogInterface.BUTTON_NEUTRAL:
                    if (mNeutralButtonListener != null) {
                        mNeutralButtonListener.onClick(v);
                    }
                    return;
                case BUTTON_ICON:
                    if (mCloseButtonListener != null) {
                        mCloseButtonListener.onClick(v);
                    }
                    return;
                default:
                    throw new IllegalArgumentException("Button does not exist");
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.CustomDialogAnimation;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUpDialogSize();
    }

    /**
     * Set this flag to false, not to dismiss modal on button click
     */
    public void setModalDismissFlag(boolean shouldDismissModal) {
        this.shouldDismissModal = shouldDismissModal;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(dialog);
        }
    }
}