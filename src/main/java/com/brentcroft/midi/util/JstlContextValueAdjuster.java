package com.brentcroft.midi.util;

import com.brentcroft.tools.jstl.MapBindings;

import static com.brentcroft.tools.jstl.MapBindings.jstl;

public class JstlContextValueAdjuster implements ContextValueMapper
{
    private final MapBindings bindings;

    public JstlContextValueAdjuster( MapBindings bindings )
    {
        this.bindings = bindings;
    }

    @Override
    public ContextValueMapper put( String s, Object o )
    {
        bindings.put( s, o );
        return this;
    }

    @Override
    public ContextValueMapper inContext()
    {
        return new JstlContextValueAdjuster( new MapBindings( bindings ) );
    }

    @Override
    public String map( String key, String value )
    {
        return jstl().expandText( value, bindings );
    }
}