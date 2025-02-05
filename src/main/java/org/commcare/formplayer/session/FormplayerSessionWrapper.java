package org.commcare.formplayer.session;

import org.commcare.core.interfaces.RemoteInstanceFetcher;
import org.commcare.core.interfaces.UserSandbox;
import org.commcare.core.process.CommCareInstanceInitializer;
import org.commcare.formplayer.sandbox.UserSqlSandbox;
import org.commcare.modern.session.SessionWrapper;
import org.commcare.session.CommCareSession;
import org.commcare.session.SessionFrame;
import org.commcare.util.CommCarePlatform;

/**
 * Created by willpride on 1/29/16.
 */
class FormplayerSessionWrapper extends SessionWrapper {

    public FormplayerSessionWrapper(CommCarePlatform platform, UserSandbox sandbox,
                                    RemoteInstanceFetcher remoteInstanceFetcher)
            throws RemoteInstanceFetcher.RemoteInstanceException {
        this(platform, sandbox, new SessionFrame(), remoteInstanceFetcher);
    }

    public FormplayerSessionWrapper(CommCarePlatform platform, UserSandbox sandbox, SessionFrame sessionFrame,
                                    RemoteInstanceFetcher remoteInstanceFetcher)
            throws RemoteInstanceFetcher.RemoteInstanceException {
        super(platform, sandbox);
        this.frame = sessionFrame;
        prepareExternalSources(remoteInstanceFetcher);
    }

    public FormplayerSessionWrapper(CommCareSession session, CommCarePlatform platform, UserSandbox sandbox,
                                    RemoteInstanceFetcher remoteInstanceFetcher)
            throws RemoteInstanceFetcher.RemoteInstanceException {
        super(session, platform, sandbox);
        prepareExternalSources(remoteInstanceFetcher);
    }


    @Override
    public CommCareInstanceInitializer getIIF() {
        if (initializer == null) {
            initializer = new FormplayerInstanceInitializer(this, (UserSqlSandbox)mSandbox, mPlatform);
        }
        return initializer;
    }
}
