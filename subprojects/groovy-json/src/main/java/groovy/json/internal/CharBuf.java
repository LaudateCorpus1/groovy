/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Derived from Boon all rights granted to Groovy project for this fork.
 */
package groovy.json.internal;

import groovy.json.*;
import sun.nio.cs.Surrogate;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

import static groovy.json.internal.Exceptions.die;

/**
 * @author Rick Hightower
 */
public class CharBuf extends Writer implements CharSequence {
    protected int capacity = 16;
    protected int location = 0;

    protected char[] buffer;


    public CharBuf( char[] buffer ) {
        this.buffer = buffer;
        this.capacity = buffer.length;
    }

    public CharBuf( byte[] bytes ) {
        this.buffer = null;
        this.addAsUTF( bytes );
    }

    public static CharBuf createExact( final int capacity ) {
        return new CharBuf( capacity ) {
            public CharBuf add( char[] chars ) {
                Chr._idx( buffer, location, chars );
                location += chars.length;
                return this;
            }
        };
    }

    public static CharBuf create( int capacity ) {
        return new CharBuf( capacity );
    }

    public static CharBuf create( char[] buffer ) {
        return new CharBuf( buffer );
    }

    protected CharBuf( int capacity ) {
        this.capacity = capacity;
        init();
    }


    protected CharBuf() {
        init();
    }


    public void write( char[] cbuf, int off, int len ) {

        if ( off == 0 && cbuf.length == len ) {
            this.add( cbuf );
        } else {
            char[] buffer = ArrayUtils.copyRange( cbuf, off, off + len );
            this.add( buffer );
        }

    }


    public void flush() throws IOException {
    }


    public void close() throws IOException {
    }

    public void init() {
        buffer = new char[ capacity ];
    }

    public final CharBuf add( String str ) {
        add( FastStringUtils.toCharArray( str ) );
        return this;
    }


    public final CharBuf addString( String str ) {
        add( FastStringUtils.toCharArray( str ) );
        return this;
    }


    public final CharBuf add( int i ) {

        add( Integer.toString( i ) );
        return this;
    }


    private Cache<Integer, char[]> icache;

    public final CharBuf addInt( int i ) {
        switch ( i ) {
            case 0:
                addChar( '0' );
                return this;
            case 1:
                addChar( '1' );
                return this;
            case -1:
                addChar( '-' );
                addChar( '1' );
                return this;
        }

        addInt( Integer.valueOf( i ) );
        return this;
    }

    public final CharBuf addInt( Integer key ) {


        if ( icache == null ) {
            icache = new SimpleCache<Integer, char[]>( 20, CacheType.LRU );
        }
        char[] chars = icache.get( key );

        if ( chars == null ) {
            String str = Integer.toString( key );
            chars = FastStringUtils.toCharArray( str );
            icache.put( key, chars );
        }

        addChars( chars );
        return this;
    }


    final char[] trueChars = "true".toCharArray();
    final char[] falseChars = "false".toCharArray();

    public final CharBuf add( boolean b ) {
        addChars( b ? trueChars : falseChars );
        return this;
    }


    public final CharBuf addBoolean( boolean b ) {

        add( Boolean.toString( b ) );
        return this;
    }

    public final CharBuf add( byte i ) {

        add( Byte.toString( i ) );
        return this;
    }

    public final CharBuf addByte( byte i ) {

        addInt( i );
        return this;
    }


    public final CharBuf add( short i ) {

        add( Short.toString( i ) );
        return this;
    }


    public final CharBuf addShort( short i ) {

        addInt( i );
        return this;
    }

    public final CharBuf add( long l ) {
        add( Long.toString( l ) );
        return this;
    }


    public final CharBuf add( double d ) {
        add( Double.toString( d ) );
        return this;
    }


    private Cache<Double, char[]> dcache;


    public final CharBuf addDouble( double d ) {
        addDouble( Double.valueOf( d ) );
        return this;
    }

    public final CharBuf addDouble( Double key ) {

        if ( dcache == null ) {
            dcache = new SimpleCache<Double, char[]>( 20, CacheType.LRU );
        }
        char[] chars = dcache.get( key );

        if ( chars == null ) {
            String str = Double.toString( key );
            chars = FastStringUtils.toCharArray( str );
            dcache.put( key, chars );
        }

        add( chars );
        return this;
    }


    public final CharBuf add( float d ) {
        add( Float.toString( d ) );
        return this;
    }


    private Cache<Float, char[]> fcache;

    public final CharBuf addFloat( float d ) {
        addFloat( Float.valueOf( d ) );
        return this;
    }


    public final CharBuf addFloat( Float key ) {

        if ( fcache == null ) {
            fcache = new SimpleCache<Float, char[]>( 20, CacheType.LRU );
        }
        char[] chars = fcache.get( key );

        if ( chars == null ) {
            String str = Float.toString( key );
            chars = FastStringUtils.toCharArray( str );
            fcache.put( key, chars );
        }

        add( chars );

        return this;
    }

    public final CharBuf addChar( byte i ) {
        add( ( char ) i );
        return this;
    }


    public final CharBuf addChar( int i ) {
        add( ( char ) i );
        return this;
    }


    public final CharBuf addChar( short i ) {
        add( ( char ) i );
        return this;
    }


    public final CharBuf addChar( final char ch ) {

        int _location = location;
        char[] _buffer = buffer;
        int _capacity = capacity;

        if ( 1 + _location > _capacity ) {
            _buffer = Chr.grow( _buffer );
            _capacity = _buffer.length;

        }

        _buffer[ _location ] = ch;
        _location++;


        location = _location;
        buffer = _buffer;
        capacity = _capacity;
        return this;
    }

    public CharBuf addLine( String str ) {
        add( str.toCharArray() );
        add( '\n' );
        return this;
    }


    public CharBuf addLine( CharSequence str ) {
        add( str.toString() );
        add( '\n' );
        return this;
    }

    public CharBuf add( char[] chars ) {
        if ( chars.length + location > capacity ) {
            buffer = Chr.grow( buffer, buffer.length * 2 + chars.length );
            capacity = buffer.length;
        }

        Chr._idx( buffer, location, chars );
        location += chars.length;
        return this;
    }


    public final CharBuf addChars( char[] chars ) {
        if ( chars.length + location > capacity ) {
            buffer = Chr.grow( buffer, buffer.length * 2 + chars.length );
            capacity = buffer.length;
        }

        arraycopy( chars, 0, buffer, location, chars.length );
        location += chars.length;
        return this;
    }


    public final CharBuf addQuoted( char[] chars ) {

        int _location = location;
        char[] _buffer = buffer;
        int _capacity = capacity;

        int sizeNeeded = chars.length + 2 + _location;
        if ( sizeNeeded > _capacity ) {
            _buffer = Chr.grow( _buffer, sizeNeeded * 2 );
            _capacity = _buffer.length;
        }
        _buffer[ _location ] = '"';
        _location++;

        arraycopy( chars, 0, _buffer, _location, chars.length );

        _location += ( chars.length );
        _buffer[ _location ] = '"';
        _location++;

        location = _location;
        buffer = _buffer;
        capacity = _capacity;
        return this;
    }


    public final CharBuf addJsonEscapedString( String jsonString ) {
        char[] charArray = FastStringUtils.toCharArray( jsonString );
        return addJsonEscapedString( charArray );

    }


    private static boolean isJSONControl( int c ) {
        /* Anything less than space is a control character. */
        if ( c < 30 ) {
            return true;
        /* 34 is double quote. */
        } else if ( c == 34 ) {
            return true;
        } else if ( c == 92 ) {
            return true;
        }
        return false;
    }

    private static boolean hasAnyJSONControlChars( final char[] charArray ) {

        int index = 0;
        char c;
        while ( true ) {
            c = charArray[ index ];
            if ( isJSONControl( c ) ) {
                return true;
            }
            if ( ++index >= charArray.length ) return false;
        }

    }

    public final CharBuf addJsonEscapedString( final char[] charArray ) {
        if ( charArray.length == 0 ) return this;
        if ( hasAnyJSONControlChars( charArray ) ) {
            return doAddJsonEscapedString( charArray );
        } else {
            return this.addQuoted( charArray );
        }
    }

    private final CharBuf doAddJsonEscapedString( char[] charArray ) {

        char[] _buffer = buffer;
        int _location = this.location;
        int ensureThisMuch = charArray.length * 2;

        int sizeNeeded = ( ensureThisMuch ) + _location;
        if ( sizeNeeded > capacity ) {

            int growBy = ( _buffer.length * 2 ) < sizeNeeded ? sizeNeeded : ( _buffer.length * 2 );
            _buffer = Chr.grow( buffer, growBy );
            capacity = _buffer.length;
        }


        _buffer[ _location ] = '"';
        _location++;

        int index = 0;
        while ( true ) {
            char c = charArray[ index ];


            if ( isJSONControl( c ) ) {

                switch ( c ) {
                    case '\"':
                        _buffer[ _location ] = '\\';
                        _location++;
                        _buffer[ _location ] = '"';
                        _location++;
                        break;
                    case '\\':
                        _buffer[ _location ] = '\\';
                        _location++;
                        _buffer[ _location ] = '\\';
                        _location++;
                        break;
                    case '\b':
                        _buffer[ _location ] = '\\';
                        _location++;
                        _buffer[ _location ] = 'b';
                        _location++;
                        break;
                    case '\f':
                        _buffer[ _location ] = '\\';
                        _location++;
                        _buffer[ _location ] = 'f';
                        _location++;
                        break;
                    case '\n':
                        _buffer[ _location ] = '\\';
                        _location++;
                        _buffer[ _location ] = 'n';
                        _location++;
                        break;
                    case '\r':
                        _buffer[ _location ] = '\\';
                        _location++;
                        _buffer[ _location ] = 'r';
                        _location++;
                        break;

                    case '\t':
                        _buffer[ _location ] = '\\';
                        _location++;
                        _buffer[ _location ] = 't';
                        _location++;
                        break;
                }
            } else {

                _buffer[ _location ] = c;
                _location++;

            }


            if ( ++index >= charArray.length ) break;


        }
        _buffer[ _location ] = '"';
        _location++;


        buffer = _buffer;
        location = _location;

        return this;
    }


    public final CharBuf addJsonFieldName( String str ) {
        return addJsonFieldName( FastStringUtils.toCharArray( str ) );
    }

    public final CharBuf addJsonFieldName( char[] chars ) {
        int _location = location;
        char[] _buffer = buffer;
        int _capacity = capacity;

        try {


            int sizeNeeded = chars.length + 3 + _location;
            if ( sizeNeeded > _capacity ) {
                _buffer = Chr.grow( _buffer, sizeNeeded * 2 );
                _capacity = _buffer.length;
            }
            _buffer[ _location ] = '"';
            _location++;

            arraycopy( chars, 0, _buffer, _location, chars.length );

            _location += ( chars.length );
            _buffer[ _location ] = '"';
            _location++;
            _buffer[ _location ] = ':';
            _location++;

            location = _location;
            buffer = _buffer;
            capacity = _capacity;
            return this;
        } catch ( Exception ex ) {
            return Exceptions.handle( CharBuf.class, Exceptions.sputs( toDebugString(), new String( chars ), "_location", _location ), ex );
        }
    }

    public final CharBuf addQuoted( String str ) {
        final char[] chars = FastStringUtils.toCharArray( str );
        addQuoted( chars );
        return this;
    }


    public CharBuf add( char[] chars, final int length ) {
        if ( length + location < capacity ) {
            Chr._idx( buffer, location, chars, length );
        } else {
            buffer = Chr.grow( buffer, buffer.length * 2 + length );
            Chr._idx( buffer, location, chars );
            capacity = buffer.length;
        }
        location += length;
        return this;
    }

    public CharBuf add( byte[] chars ) {
        if ( chars.length + location < capacity ) {
            Chr._idx( buffer, location, chars );
        } else {
            buffer = Chr.grow( buffer, buffer.length * 2 + chars.length );
            Chr._idx( buffer, location, chars );
            capacity = buffer.length;
        }
        location += chars.length;
        return this;
    }


    private final static void sysstemarraycopy( final char[] src, final int srcPos, final char[] dest, final int destPos, final int length ) {
        System.arraycopy( src, srcPos, dest, destPos, length );

    }

    private final static void directArraycopy( final char[] src, final int srcPos, final char[] dest, final int destPos, final int length ) {
        int destIndex = destPos;
        for ( int index = srcPos; index < srcPos + length; index++, destIndex++ ) {
            dest[ destIndex ] = src[ index ];
        }
    }

    private final static void arraycopy( final char[] src, final int srcPos, final char[] dest, final int destPos, final int length ) {

        sysstemarraycopy( src, srcPos, dest, destPos, length );

    }


    public CharBuf add( byte[] bytes, int start, int end ) {
        int charsLength = end - start;
        if ( charsLength + location > capacity ) {
            buffer = Chr.grow( buffer, buffer.length * 2 + charsLength );
        }
        Chr._idx( buffer, location, bytes, start, end );
        capacity = buffer.length;
        location += charsLength;
        return this;
    }


    public final CharBuf add( char ch ) {
        if ( 1 + location < capacity ) {
            buffer[ location ] = ch;
        } else {
            buffer = Chr.grow( buffer );
            buffer[ location ] = ch;
            capacity = buffer.length;
        }
        location += 1;
        return this;
    }


    public int length() {
        return len();
    }


    public char charAt( int index ) {
        return buffer[ index ];
    }


    public CharSequence subSequence( int start, int end ) {
        return new String( buffer, start, end - start );
    }

    public String toString() {
        return new String( buffer, 0, location );
    }

    public String toDebugString() {
        return "CharBuf{" +
                "capacity=" + capacity +
                ", location=" + location +
                '}';
    }

    public String toStringAndRecycle() {

        String str = new String( buffer, 0, location );
        location = 0;
        return str;
    }

    public int len() {
        return location;
    }

    public char[] toCharArray() {
        return this.buffer;
    }

    public void _len( int location ) {
        this.location = location;
    }


    public char[] readForRecycle() {
        this.location = 0;
        return this.buffer;
    }


    public void recycle() {
        this.location = 0;
    }


    private static double powersOf10[] = {
            1.0,
            10.0,
            100.0,
            1000.0,
            10000.0,
            100000.0,
            1000000.0,
            10000000.0,
            100000000.0,
            1000000000.0,
            10000000000.0,
            100000000000.0,
            1000000000000.0,
            10000000000000.0,
            100000000000000.0,
    };


    private static float fpowersOf10[] = {
            1.0f,
            10.0f,
            100.0f,
            1000.0f,
            10000.0f,
            100000.0f,
            1000000.0f,
            10000000.0f,
            100000000.0f,
            1000000000.0f,
    };

    public double doubleValue() {

        boolean simple = true;
        int digitsPastPoint = 0;
        boolean foundPoint = false;
        boolean negative = false;
        int startIndex = 0;

        double sign;

        if ( buffer[ startIndex ] == '-' ) {
            startIndex++;
            negative = true;
            sign = -1.0;
        } else {
            negative = false;
            sign = 1.0;
        }

        loop:
        for ( int index = startIndex; index < location; index++ ) {
            char ch = buffer[ index ];
            switch ( ch ) {
                case 'e':
                    simple = false;
                    break loop;
                case 'E':
                    simple = false;
                    break loop;
                case 'F':
                    simple = false;
                    break loop;
                case 'f':
                    simple = false;
                    break loop;
                case '.':
                    foundPoint = true;
                    continue loop;
            }
            if ( foundPoint ) {
                digitsPastPoint++;
                if ( digitsPastPoint >= powersOf10.length ) {
                    simple = true;
                    break;
                }
            }
        }

        if ( simple ) {
            long value;
            final int length = location - startIndex;

            if ( CharScanner.isInteger( buffer, startIndex, length, negative ) ) {
                value = CharScanner.parseIntIgnoreDot( buffer, startIndex, length );
            } else {
                value = CharScanner.parseLongIgnoreDot( buffer, startIndex, length );
            }
            if ( digitsPastPoint < powersOf10.length ) {
                double power = powersOf10[ digitsPastPoint ] * sign;
                return value / power;

            }


        }

        return Double.parseDouble( toString() ) * sign;
    }


    public float floatValue() {

        boolean simple = true;
        int digitsPastPoint = 0;
        boolean foundPoint = false;

        float sign;
        int startIndex = 0;

        if ( buffer[ startIndex ] == '-' ) {
            startIndex++;
            sign = -1.0f;
        } else {
            sign = 1.0f;
        }


        int length = location - startIndex;
        if ( length > 10 ) {
            return Float.parseFloat( toString() ) * sign;
        }
        loop:
        for ( int index = startIndex; index < location; index++ ) {
            char ch = buffer[ index ];
            switch ( ch ) {
                case 'e':
                    simple = false;
                    break loop;
                case 'E':
                    simple = false;
                    break loop;
                case 'F':
                    simple = false;
                    break loop;
                case 'f':
                    simple = false;
                    break loop;
                case '.':
                    foundPoint = true;
                    continue loop;
            }
            if ( foundPoint ) {
                digitsPastPoint++;
                if ( digitsPastPoint >= powersOf10.length ) {
                    simple = true;
                    break;
                }
            }
        }

        if ( simple ) {
            int value;

            value = CharScanner.parseIntIgnoreDot( buffer, startIndex, length );
            if ( digitsPastPoint < powersOf10.length ) {
                float power = fpowersOf10[ digitsPastPoint ] * sign;
                return value / power;

            }


        }

        return Float.parseFloat( toString() ) * sign;

    }

    public int intValue() {
        int sign = 1;
        int startIndex = 0;
        if ( buffer[ startIndex ] == '-' ) {
            startIndex++;
            sign = -1;

        }
        return CharScanner.parseInt( buffer, startIndex, location - startIndex ) * sign;
    }

    public long longValue() {
        long sign = 1;
        int startIndex = 0;

        if ( buffer[ startIndex ] == '-' ) {
            startIndex++;
            sign = -1;

        }
        return CharScanner.parseLong( buffer, startIndex, location - startIndex ) * sign;
    }


    public byte byteValue() {
        return ( byte ) intValue();
    }

    public short shortValue() {
        return ( short ) intValue();
    }


    public Number toIntegerWrapper() {

        int sign = 1;
        boolean negative = false;
        int startIndex = 0;
        if ( buffer[ startIndex ] == '-' ) {
            startIndex++;
            sign = -1;
            negative = true;

        }


        if ( CharScanner.isInteger( buffer, startIndex, location - startIndex, negative ) ) {
            return intValue() * sign;
        } else {
            return longValue() * sign;
        }

    }


    public void addAsUTF( byte[] value ) {


        if ( this.buffer == null ) {
            this.buffer = new char[ value.length * 2 ];
            capacity = buffer.length;
        } else if ( this.buffer.length < value.length ) {
            buffer = Chr.grow( buffer, value.length - buffer.length );
            capacity = buffer.length;
        }

        char[] buffer = this.buffer;
        int location = this.location;

        for ( int index = 0; index < value.length; index++ ) {
            int c = value[ index ];


            if ( c >= 0 ) {
                buffer[ location ] = ( char ) c;
                location++;
            } else {
                this.location = location;
                index = utf8MultiByte( c, index, value );
                location = this.location;
            }

        }

        this.location = location;


    }


    //  [C2..DF] [80..BF]
    private static boolean isMalformed2( int b1, int b2 ) {
        return ( b1 & 0x1e ) == 0x0 || ( b2 & 0xc0 ) != 0x80;
    }

    //  [E0]     [A0..BF] [80..BF]
    //  [E1..EF] [80..BF] [80..BF]
    private static boolean isMalformed3( int b1, int b2, int b3 ) {
        return ( b1 == ( byte ) 0xe0 && ( b2 & 0xe0 ) == 0x80 ) ||
                ( b2 & 0xc0 ) != 0x80 || ( b3 & 0xc0 ) != 0x80;
    }

    //  [F0]     [90..BF] [80..BF] [80..BF]
    //  [F1..F3] [80..BF] [80..BF] [80..BF]
    //  [F4]     [80..8F] [80..BF] [80..BF]
    //  only check 80-be range here, the [0xf0,0x80...] and [0xf4,0x90-...]
    //  will be checked by Surrogate.neededFor(uc)
    private static boolean isMalformed4( int b2, int b3, int b4 ) {
        return ( b2 & 0xc0 ) != 0x80 || ( b3 & 0xc0 ) != 0x80 ||
                ( b4 & 0xc0 ) != 0x80;
    }


    private final int utf8MultiByte( final int c, int index, byte[] bytes ) {


        int location = this.location;
        char[] buffer = this.buffer;

        //boolean ok = true;

        if ( ( c >> 5 ) == -2 ) {
            int b2;

            //ok = index + 1 < bytes.length || die( "unable to parse 2 byte utf 8 - b2" );
            index++;
            b2 = bytes[ index ];

            if ( isMalformed2( c, b2 ) ) {

                buffer[ location ] = '#';
                location++;

            } else {
                buffer[ location ] = ( char ) ( ( ( c << 6 ) ^ b2 ) ^ 0x0f80 );
                location++;

            }
        } else if ( ( c >> 4 ) == -2 ) {
            int b2;
            int b3;

            //ok = index + 1 < bytes.length || die( "unable to parse 3 byte utf 8 - b2" );
            index++;
            b2 = bytes[ index ];
            //ok = index + 1 < bytes.length || die( "unable to parse 3 byte utf 8 - b3" );
            index++;
            b3 = bytes[ index ];

            if ( isMalformed3( c, b2, b3 ) ) {
                buffer[ location ] = '#';
                location++;
            } else {
                buffer[ location ] = ( char ) ( ( ( c << 12 ) ^ ( b2 << 6 ) ^ b3 ) ^ 0x1f80 );
                location++;
            }
        } else if ( ( c >> 3 ) == -2 ) {
            int b2;
            int b3;
            int b4;

            //ok = index + 1 < bytes.length || die( "unable to parse 4 byte utf 8 - b2" );
            index++;
            b2 = bytes[ index ];
            //ok = index + 1 < bytes.length || die( "unable to parse 4 byte utf 8 - b3" );
            index++;
            b3 = bytes[ index ];
            //ok = index + 1 < bytes.length || die( "unable to parse 4 byte utf 8 - b4" );
            index++;
            b4 = bytes[ index ];

            int uc = ( ( c & 0x07 ) << 18 ) |
                    ( ( b2 & 0x3f ) << 12 ) |
                    ( ( b3 & 0x3f ) << 6 ) |
                    ( b4 & 0x3f );

            if ( isMalformed4( b2, b3, b4 ) && !Surrogate.neededFor( uc ) ) {
                addChar( '#' );
            } else {

                final char high = Surrogate.high( uc );
                final char low = Surrogate.low( uc );

                addChar( high );
                addChar( low );

            }
        }


        this.location = location;
        this.buffer = buffer;
        return index;
    }


    final static char[] nullChars = "null".toCharArray();

    public final void addNull() {
        this.add( nullChars );
    }

    public void removeLastChar() {
        location--;
    }


    private Cache<BigDecimal, char[]> bigDCache;

    public CharBuf addBigDecimal( BigDecimal key ) {
        if ( bigDCache == null ) {
            bigDCache = new SimpleCache<BigDecimal, char[]>( 20, CacheType.LRU );
        }
        char[] chars = bigDCache.get( key );

        if ( chars == null ) {
            String str = key.toString();
            chars = FastStringUtils.toCharArray( str );
            bigDCache.put( key, chars );
        }

        add( chars );

        return this;


    }

    private Cache<BigInteger, char[]> bigICache;

    public CharBuf addBigInteger( BigInteger key ) {
        if ( bigICache == null ) {
            bigICache = new SimpleCache<BigInteger, char[]>( 20, CacheType.LRU );
        }
        char[] chars = bigICache.get( key );

        if ( chars == null ) {
            String str = key.toString();
            chars = FastStringUtils.toCharArray( str );
            bigICache.put( key, chars );
        }

        add( chars );

        return this;

    }


    private Cache<Long, char[]> lcache;


    public final CharBuf addLong( long l ) {

        addLong( Long.valueOf( l ) );
        return this;
    }

    public final CharBuf addLong( Long key ) {

        if ( lcache == null ) {
            lcache = new SimpleCache<Long, char[]>( 20, CacheType.LRU );
        }
        char[] chars = lcache.get( key );

        if ( chars == null ) {
            String str = Long.toString( key );
            chars = FastStringUtils.toCharArray( str );
            lcache.put( key, chars );
        }

        add( chars );

        return this;
    }


    public final CharBuf decodeJsonString( char[] chars ) {
        return decodeJsonString( chars, 0, chars.length );
    }

    public final CharBuf decodeJsonString( char[] chars, int start, int to ) {
        int len = to - start;

        char[] buffer = this.buffer;
        int location = this.location;

        if ( len > capacity ) {
            buffer = Chr.grow( buffer, buffer.length * 2 + len );
            capacity = buffer.length;
        }

        for ( int index = start; index < to; index++ ) {
            char c = chars[ index ];
            if ( c == '\\' ) {
                if ( index < to ) {
                    index++;
                    c = chars[ index ];
                    switch ( c ) {

                        case 'n':
                            buffer[ location++ ] = '\n';
                            break;

                        case '/':
                            buffer[ location++ ] = '/';
                            break;

                        case '"':
                            buffer[ location++ ] = '"';
                            break;

                        case 'f':
                            buffer[ location++ ] = '\f';
                            break;

                        case 't':
                            buffer[ location++ ] = '\t';
                            break;

                        case '\\':
                            buffer[ location++ ] = '\\';
                            break;

                        case 'b':
                            buffer[ location++ ] = '\b';
                            break;

                        case 'r':
                            buffer[ location++ ] = '\r';
                            break;

                        case 'u':

                            if ( index + 4 < to ) {
                                String hex = new String( chars, index + 1, 4 );
                                char unicode = ( char ) Integer.parseInt( hex, 16 );
                                buffer[ location++ ] = unicode;
                                index += 4;
                            }
                            break;
                        default:
                            throw new JsonException( "Unable to decode string" );
                    }
                }
            } else {
                buffer[ location++ ] = c;
            }
        }


        this.buffer = buffer;
        this.location = location;

        return this;

    }


}


