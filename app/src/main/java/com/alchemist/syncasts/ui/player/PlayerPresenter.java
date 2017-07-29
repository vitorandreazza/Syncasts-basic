package com.alchemist.syncasts.ui.player;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.alchemist.syncasts.data.inject.ConfigPersistentScope;
import com.alchemist.syncasts.services.PlaybackService;
import com.alchemist.syncasts.ui.BasePresenter;

import javax.inject.Inject;

import rx.subscriptions.CompositeSubscription;

@ConfigPersistentScope
public class PlayerPresenter extends BasePresenter<PlayerMvpView> {

    private final CompositeSubscription mCompositeSubscription;
    private PlaybackService mPlaybackService;
    private boolean mIsServiceBound;
    private ServiceConnection mConnection;

    @Inject
    public PlayerPresenter() {
        mCompositeSubscription = new CompositeSubscription();
        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  Because we have bound to a explicit
                // service that we know is running in our own process, we can
                // cast its IBinder to a concrete class and directly access it.
                mPlaybackService = ((PlaybackService.LocalBinder) service).getService();
                checkViewAttached();
                getMvpView().onPlaybackServiceBound(mPlaybackService);
                getMvpView().onPlayStatusChanged(mPlaybackService.isPlaying() || mPlaybackService.isPreparingAsync());
                getMvpView().playPodcast();
            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                // Because it is running in our same process, we should never
                // see this happen.
                mPlaybackService = null;
                checkViewAttached();
                getMvpView().onPlaybackServiceUnbound();
            }
        };
    }

    @Override
    public void detachView() {
        super.detachView();
        mCompositeSubscription.clear();
    }

    public void setupPlaybackService() {
        checkViewAttached();
        getMvpView().startPlaybackService();
        getMvpView().bindPlaybackService(mConnection);
        mIsServiceBound = true;
    }

    public void unbindPlaybackService() {
        checkViewAttached();
        if (mIsServiceBound) {
            getMvpView().unbindPlaybackService(mConnection);
            getMvpView().onPlaybackServiceUnbound();
            mIsServiceBound = false;
        }
    }
}
