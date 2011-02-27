package org.netling.ssh;

public class AndroidConfig extends DefaultConfig {

    @Override
    protected void initRandomFactory(boolean ignored) {
        setRandomFactory(new SingletonRandomFactory(new JCERandom.Factory()));
    }       

}
