package com.alchemist.syncasts.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.ResultReceiver;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.alchemist.syncasts.BuildConfig;

import java.lang.reflect.Method;

import timber.log.Timber;

public final class Utils {

    public static void assertTrue(boolean condition) {
        if (BuildConfig.DEBUG && !condition) {
            throw new AssertionError();
        }
    }

    public static Transition findTransition(@NonNull TransitionSet set,
                                            @NonNull Class<? extends Transition> clazz,
                                            @IdRes int targetId) {
        for (int i = 0; i < set.getTransitionCount(); i++) {
            Transition transition = set.getTransitionAt(i);
            if (transition.getClass() == clazz && transition.getTargetIds().contains(targetId)) {
                return transition;
            }
            if (transition instanceof TransitionSet) {
                Transition child = findTransition((TransitionSet) transition, clazz, targetId);
                if (child != null) return child;
            }
        }
        return null;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static boolean isServiceRunning(Context context, Class serviceClass) {
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void showIme(@NonNull View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService
                (Context.INPUT_METHOD_SERVICE);
        try {
            Method showSoftInputUnchecked = InputMethodManager.class.getMethod(
                    "showSoftInputUnchecked", int.class, ResultReceiver.class);
            showSoftInputUnchecked.setAccessible(true);
            showSoftInputUnchecked.invoke(imm, 0, null);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static void hideIme(@NonNull View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
