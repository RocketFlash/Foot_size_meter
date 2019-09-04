package com.tryfitCamera.train;

import java.util.ArrayList;

/**
 * Created by Miha-ha on 02.07.16.
 */
public class OKResponse implements IResponse {
    @Override
    public ArrayList Args() {
        return new ArrayList();
    }

    @Override
    public String Error() {
        return null;
    }
}
