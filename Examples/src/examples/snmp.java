/*
 * Copyright (c) 2002 iReasoning Networks. All Rights Reserved.
 * 
 * This SOURCE CODE FILE, which has been provided by iReasoning Networks as part
 * of an iReasoning Software product for use ONLY by licensed users of the product,
 * includes CONFIDENTIAL and PROPRIETARY information of iReasoning Networks.  
 *
 * USE OF THIS SOFTWARE IS GOVERNED BY THE TERMS AND CONDITIONS 
 * OF THE LICENSE STATEMENT AND LIMITED WARRANTY FURNISHED WITH
 * THE PRODUCT.
 *
 * IN PARTICULAR, YOU WILL INDEMNIFY AND HOLD IREASONING SOFTWARE, ITS
 * RELATED COMPANIES AND ITS SUPPLIERS, HARMLESS FROM AND AGAINST ANY
 * CLAIMS OR LIABILITIES ARISING OUT OF THE USE, REPRODUCTION, OR
 * DISTRIBUTION OF YOUR PROGRAMS, INCLUDING ANY CLAIMS OR LIABILITIES
 * ARISING OUT OF OR RESULTING FROM THE USE, MODIFICATION, OR
 * DISTRIBUTION OF PROGRAMS OR FILES CREATED FROM, BASED ON, AND/OR
 * DERIVED FROM THIS SOURCE CODE FILE.
 */


import java.io.*;
import java.util.*;

import com.ireasoning.protocol.Session;
import com.ireasoning.protocol.snmp.*;
import com.ireasoning.util.ParseArguments;

/**
 * This class is the base class of snmp command line programs. It provides
 * useful functionality such as parsing command line arguments, prints out
 * PDU data, etc.
 * <br>
 * Note: MIB-II is preloaded.
 */
public abstract class snmp 
{
    protected ParseArguments _parseArgs;
    
    protected int         _port;
    protected int         _version;
    protected String      _host;
    protected String      _authProtocol;
    protected String      _authPassword;
    protected int         _privProtocol = SnmpConst.DES;
    protected String      _privPassword;
    protected String      _community;
    protected String      _user;
    protected String      _context;
    protected String[]    _oids ;
    protected boolean     _isSnmpV3;
    protected boolean     _numericalOID = false;
    protected String[]    _mibFiles;
    protected String      _mibString;
    protected int         _transportLayer;
    
    /**
     * Prints out usage
     * @param programName the program name, usually the class name
     * @param allowMultipleOIDs true if allowing user to specify multiple OIDs
     */
    protected void printUsage(String programName, boolean allowMultipleOIDs)
    {
        usage(programName, allowMultipleOIDs);
        
        System.out.println( "\nwhere possible options include:");
        System.out.println( "-?\tprint this Usage");
        System.out.println( "-h\tprint this Usage");
        System.out.println( "-o\tprint oids numerically");
        System.out.println( "-c <c>\tcommunity name");
        System.out.println( "-v <1|2|3>\tspecifies snmp version to use, default is 1");
        System.out.println( "-p <p>\tport number");
        System.out.println( "-m <m>\tmib files to load. Use ',' or ';' to separate multiple files");
        System.out.println("-k <UDP|TCP>\ttransport layer, possible values are UDP or TCP");
        moreOptions();
        System.out.println( "\nSNMPv3 options:");
        System.out.println( "-u <u>\tuser name");
        if(!programName.equals("snmptrap"))
        {
            System.out.println( "-t <t>\tcontext name");
        }
        System.out.println( "-a <a>\tauthentication algorithm, one of MD5, SHA, SHA224, SHA256, SHA384 OR SHA512. MD5 by default.");
        System.out.println( "-A <A>\tauthentication password");
        System.out.println( "-x <x>\tprivacy algorithm, one of DES, 3DES, AES, AES192 or AES256. DES by default.");
        System.out.println( "-X <X>\tprivacy password");
        System.out.println( "\nExample:");
        printExample(programName);
    }

    /**
     * Prints usage lines without explanation lines
     */
    protected void usage(String programName, boolean allowMultipleOIDs)
    {
        if(allowMultipleOIDs)
        {
            System.out.println( "Usage: java " + programName + 
                    " [options...] <hostname> [<OID> ...]\n");
            System.out.println( "<OID>\tobject identifiers, .1.3 by default. Or MIB node name if MIB loaded");
        }
        else
        {
            System.out.println( "Usage: java " + programName + 
                    " [options...] <hostname> [<OID>]\n");
            System.out.println( "<OID>\tobject identifier, .1.3 by default.");
        }
    }
    
    /**
     * Prints out more options if any. It may be overridden by subclasses
     */
    protected void moreOptions()
    {
    }

    /**
     * Prints out the example usage
     */
    protected void printExample(String programName)
    {
        System.out.println( "java " + programName + " localhost .1.3");
        System.out.println( "java " + programName + " localhost");
        System.out.println( "java " + programName + 
            " localhost -v 3 -u newUser -A abc12345 -X abc12345 .1.3" );
    }
    
    /**
     * Prints SnmpPdu content to standard out
     */
    protected void print(SnmpPdu pdu)
    {
        print(pdu, System.out);    
    }
    
    /**
     * Prints SnmpPdu content to PrintStream p
     */
    protected void print(SnmpPdu pdu, PrintStream p)
    {
        if(pdu.getErrorStatus() > 0)
        {//Error occurs or end of mib view reached.
            System.out.println( "PDU error status = " + pdu.getErrorStatusString());
            return;
        }
        if(pdu.isSnmpV3AuthenticationFailed())
        {
            System.out.println( "Authentication failure. Reason:");
            printAuthFailReason(pdu);
            return;
        }
        print(pdu.getVarBinds(), p);
    }
    private void printAuthFailReason(SnmpPdu pdu)
    {
        SnmpVarBind vb = pdu.getFirstVarBind();
        if(vb != null)
        {
            printAuthFailReason(vb.getName());
        }
    }
    
    public static void printAuthFailReason(SnmpOID oid)
    {
        if(oid.equals(AgentUsmStats.USM_STATS_UNKNOWN_USER_NAMES))
        {
            System.out.println( "Reason: Unknown user name");
        }
        else if(oid.equals(AgentUsmStats.USM_STATS_DECRYPTION_ERRORS))
        {
            System.out.println( "USM decryption error");               
        }
        else if(oid.equals(AgentUsmStats.USM_STATS_WRONG_DIGESTS))
        {
            System.out.println( "Wrong digest");               
        }
        else if(oid.equals(AgentUsmStats.USM_STATS_UNKNOWN_ENGINE_IDS))
        {
            System.out.println( "Unkown engine ID");               
        }
        else if(oid.equals(AgentUsmStats.USM_STATS_NOT_IN_TIMEWINDOWS))
        {
            System.out.println( "Not in time windows");               
        }
        else if(oid.equals(AgentUsmStats.USM_STATS_UNSUPPORTED_SEC_LEVELS))
        {
            System.out.println( "Unsupported security levels");               
        }
    }

    /**
     * Prints variable binding array to standard out
     */
    protected void print(SnmpVarBind[] varbinds)
    {
        print(varbinds, System.out);
    }

    /**
     * Prints variable binding array to PrintStream p
     */
    protected void print(SnmpVarBind[] varbinds, PrintStream p)
    {
        for (int i = 0; i < varbinds.length ; i++)    
        {
            print(varbinds[i], p);
        }
    }
    
    /**
     * Prints variable binding to standard out
     */
    protected void print(SnmpVarBind var)
    {
        print(var, System.out);
    }
    
    /**
     * Prints variable binding to PrintStream p
     */
    protected void print(SnmpVarBind var, PrintStream p)
    {
        if(var == null)
        {
            return;
        }
        SnmpDataType  n = var.getName();
        SnmpDataType  v = var.getValue();
        
        if(n == null) 
        {
            p.println( "SnmpVarBind 's name is null" );
        }
        String oid = n.toString();
        String value = v.toString();

        // **** Optional, if MIB OID name translation is desired.
        if(MibUtil.isMibFileLoaded())
        {
            //Use MibUtil.translate to translate oid and its value to string
            //format
            NameValue nv = MibUtil.translate(oid, value, true);
            if(nv != null)
            {
                if(!_numericalOID)
                {
                    oid = nv.getName();
                }
                value = nv.getValue();
            }
        }

        if( v.getType() == SnmpDataType.TIMETICKS )
        {//if it's time ticks, translate it to friendly readable format
            value = ((SnmpTimeTicks) v).getTimeString();
        }
        else if( v.getType() == SnmpDataType.END_OF_MIB_VIEW )
        {
            p.println( "End of MIB reached.");
            return;
        }
        else if( v.getType() == SnmpDataType.NO_SUCH_OBJECT )
        {
            p.println( "No such object.");
            return;
        }
        else if( v.getType() == SnmpDataType.NO_SUCH_INSTANCE )
        { 
            p.println( "No such Instance.");
            return;
        }
        p.println( oid + "\r\nValue (" + v.getTypeString() +
                "): "+ value + "\r\n");
    }

    /**
     * Creates a new instance of ParseArguments
     */
    protected ParseArguments newParseArgumentsInstance(String[] args)
    {
        return new ParseArguments(args, "?ho", "utvaAXxcpmk");
    }
    
    /**
     * Parses command options, allowing multiple OIDs and no command line
     * options
     * @see #parseOptions(String[] args, String programName, boolean allowMulitpleOIDs, boolean allowNoOption)
     */
    protected void parseOptions(String[] args, String program)
    {
        parseOptions(args, program, true, false);
    }

    /**
     * Parses command options
     * @param args the command arguments
     * @param programName the program name, usually the class name
     * @param allowMultipleOIDs true if allowing user to specify multiple OIDs
     * @param allowNoOption true if it's ok if no command line options is
     * supplied.
     */
    protected void parseOptions(String[] args, 
                                String programName, 
                                boolean allowMultipleOIDs,
                                boolean allowNoOption)
    {
        _parseArgs = newParseArgumentsInstance(args);
        if(_parseArgs.isSwitchPresent('?') ||
           _parseArgs.isSwitchPresent('h') ||
           (!allowNoOption && args.length == 0))
        {//for help, print usage.
            printUsage(programName, allowMultipleOIDs);
            System.exit(0);
        }
        
        _numericalOID = _parseArgs.isSwitchPresent('o');
        _version = Integer.parseInt(_parseArgs.getOptionValue('v', "1")) ;//default is SNMPv1
        if( _version < 3) _version --;
        _isSnmpV3 = ( _version == SnmpConst.SNMPV3 );

        SnmpSession.loadMib2();//preload MIB-II (RFC1213) module
        // **** Optional, if MIB OID name translation is desired.
        _mibString = _parseArgs.getOptionValue('m');
        if(_mibString != null && _mibString.length() > 0)
        {//load MIB files
            _mibFiles  = parseMibFiles(_mibString);
            for (int i = 0; i < _mibFiles.length ; i++) 
            {
                try
                {
                    System.out.println( "Load MIB: " + _mibFiles[i]);
                    SnmpSession.loadMib(_mibFiles[i]);
                }
                catch(java.text.ParseException pe)
                {
                    System.out.println( "MIB file (" + _mibFiles[i] + ") has syntax errors");
                }
                catch(IOException ie)
                {
                    System.out.println( "Can't load mib file: " +
                            _mibFiles[i]);
                }
            }
        }

        if(_isSnmpV3)
        {//parse SNMPv3 options
            _user = _parseArgs.getOptionValue('u');
            if(_user == null)
            {
                throw new RuntimeException("User name has to be provided.");
            }
            _context = _parseArgs.getOptionValue('t');
            _authProtocol = _parseArgs.getOptionValue('a', "MD5");//md5 is the default
            if(_authProtocol.equalsIgnoreCase("md5"))
            {
                _authProtocol = SnmpConst.MD5;
            }
            else if(_authProtocol.equalsIgnoreCase("sha"))
            {
                _authProtocol = SnmpConst.SHA;
            }
            else if(_authProtocol.equalsIgnoreCase("sha224"))
            {
                _authProtocol = SnmpConst.SHA224;
            }
            else if(_authProtocol.equalsIgnoreCase("sha256"))
            {
                _authProtocol = SnmpConst.SHA256;
            }
            else if(_authProtocol.equalsIgnoreCase("sha384"))
            {
                _authProtocol = SnmpConst.SHA384;
            }
            else if(_authProtocol.equalsIgnoreCase("sha512"))
            {
                _authProtocol = SnmpConst.SHA512;
            }
            else
            {
                throw new RuntimeException("Unknown authentication algorithm.");
            }
            String s = _parseArgs.getOptionValue('x', "des");//des is the default
            if(s.equalsIgnoreCase("des"))
            {
                _privProtocol = SnmpConst.DES;
            }
            else if(s.equalsIgnoreCase("3des"))
            {
                _privProtocol = SnmpConst.TRIPLE_DES;
            }
            else if(s.equalsIgnoreCase("aes"))
            {
                _privProtocol = SnmpConst.AES;
            }
            else if(s.equalsIgnoreCase("aes192"))
            {
                _privProtocol = SnmpConst.AES192;
            }
            else if(s.equalsIgnoreCase("aes256"))
            {
                _privProtocol = SnmpConst.AES256;
            }
            else
            {
                throw new RuntimeException("Unknown privacy algorithm.");
            }
            _authPassword = _parseArgs.getOptionValue('A');
            _privPassword = _parseArgs.getOptionValue('X');
        }
        _community = _parseArgs.getOptionValue('c', "public");
        _port = Integer.parseInt(_parseArgs.getOptionValue('p', "161"));
        String transport = _parseArgs.getOptionValue('k', "UDP");
        _transportLayer = "UDP".equalsIgnoreCase(transport) ? Session.UDP : Session.TCP;
        parseArgs();
    }

    /**
     * Parses non-option arguments
     */
    protected void parseArgs()
    {
        String [] as = _parseArgs.getArguments();
        if(as.length > 0)
        {
            _host = as[0];
        }

        if(as.length > 1)
        {
            _oids = new String[as.length - 1];
            for (int i = 1; i < as.length ; i++) 
            {
                _oids[i - 1] = as[i];
            }
        }
        else
        {
            _oids = new String[]{".1.3"};
        }
    }
    
    /**
     * Prints values of all options
     */
    protected void printOptions()
    {
        System.out.println( "Options:");
        System.out.println( "_____________________________________");
        System.out.println( "host =\t\t\t" + _host);
        System.out.println( "port =\t\t\t" + _port);
        System.out.println( "isSnmpV3 =\t\t" + _isSnmpV3);
        System.out.println( "authProtocol =\t\t" + _authProtocol);
        System.out.println( "authPassword =\t\t" + _authPassword);
        System.out.println( "privProtocol =\t\t" + _privProtocol);
        System.out.println( "privPassword =\t\t" + _privPassword);
        System.out.println( "community =\t\t" + _community);
        System.out.println( "user =\t\t\t" + _user);
        System.out.println( "mib files =\t\t" + _mibString);
        printMoreOptions();
        System.out.println( "_____________________________________");
    }

    /**
     * Prints values of other options if any. Can be implemented by subclasses
     */
    protected void printMoreOptions()
    {
    }

    /**
     * Parses the mib file options, which is separated by ',' or ';'
     */
    private static String[] parseMibFiles(String mibs)
    {
        StringTokenizer st = new StringTokenizer(mibs, ";,");
        int size = st.countTokens() ;
        String[] mibFiles = new String[size];
        int i = 0;
        while (st.hasMoreTokens()) 
        {
            mibFiles[i++] = st.nextToken();
        }
        return mibFiles;
    }
    

    //
    //  ------------ utility methods
    //

    /**
     * Translates to SnmpDataType object. Used in snmptrap and snmpset classes
     * @param type  one of i, u, t, a, o, s, c
     * i: INTEGER, u: unsigned INTEGER, t: TIMETICKS,
     * a: IPADDRESS, o: OBJID, s: STRING, c: counter32, g: gauge, x: hex data (in "0x1B 0xAC ..." format)
     * @param value  value 
     */
    public static SnmpDataType translate(String type, String value)
    {
        if(type.equals("i"))
        {
            return new SnmpInt(Integer.parseInt(value));
        }
        else if(type.equals("u"))
        {
            return new SnmpUInt(Long.parseLong(value));
        }
        else if(type.equals("t"))
        {
            return new SnmpTimeTicks(Long.parseLong(value));
        }
        else if(type.equals("a"))
        {
            return new SnmpIpAddress(value);
        }
        else if(type.equals("o"))
        {
            return new SnmpOID(value);
        }
        else if(type.equals("s"))
        {
            return new SnmpOctetString(value);
        }
        else if(type.equals("x"))
        {
            return new SnmpOctetString(SnmpOctetString.getBytes(value));
        }
        else if(type.equals("c"))
        {
            return new SnmpCounter32(value);
        }
        else if(type.equals("g"))
        {
            return new SnmpGauge32(value);
        }
        else
        {
            throw new RuntimeException("Unknown data type");
        }
    }

    /**
     * Helper method, returns byte array of passed hex or regular string
     */
    public static byte [] getHexString(String text)
    {
        if(text.startsWith("0x") || text.startsWith("0X"))
        {//expecting something like "0X01ACDF11"
            int len = text.length() - 2;
            if(len % 2 != 0) 
            {
                throw new IllegalArgumentException("Illegal hex string");
            }
            byte [] ret = new byte[len/2];
            int j = 0;
            for (int i = 0; i < len ; ) 
            {
                String s = text.substring(i + 2, i + 4);
                int value = Integer.parseInt(s, 16);
                ret[j++] = (byte) value;
                i += 2;
            }

            return ret;
        }
        else
        {//regular string
            return text.getBytes();
        }
    }

}
// end of class snmp
