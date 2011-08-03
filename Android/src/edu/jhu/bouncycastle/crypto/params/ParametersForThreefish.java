package edu.jhu.bouncycastle.crypto.params;

import edu.jhu.bouncycastle.crypto.CipherParameters;

public class ParametersForThreefish implements CipherParameters {
    public final static int Threefish256 = 256;
    public final static int Threefish512 = 512;
    public final static int Threefish1024 = 1024;
    
    private int stateSize;
    private CipherParameters parameters;
    private long[] tweak;

    public ParametersForThreefish(
            CipherParameters    parameters,
            int                 stateSize,
            long[]              tweak)
    {
        this.stateSize = stateSize;
        this.parameters = parameters;
        if (tweak != null) {
            this.tweak = new long[2];
            this.tweak[0] = tweak[0];
            this.tweak[1] = tweak[1];
        }
    }

    /**
     * @return the stateSize
     */
    public int getStateSize() {
        return stateSize;
    }

    /**
     * @return the parameters
     */
    public CipherParameters getParameters() {
        return parameters;
    }

    /**
     * @return the tweak
     */
    public long[] getTweak() {
        return tweak;
    }

}
