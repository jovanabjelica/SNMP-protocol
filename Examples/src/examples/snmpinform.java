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


import com.ireasoning.protocol.TimeoutException;
import com.ireasoning.protocol.snmp.*;
import com.ireasoning.util.ParseArguments;
import java.io.IOException;

/**
 * This class demonstrates two different implementations of snmp get_next operation. 
 * <br>
 * 1. Use SnmpSession.snmpGetNextRequest. This is a more convinient way. <br>
 * 2. Use primitive SnmpSession.send <br>
 * <pre>
 * Example:
 * java snmpinform -s 10333 -q .1.3.6.1.2.1.2.2.1.1 localhost
 * java snmpinform -v 3 -u newUser -A abc12345 -X abc12345 -s 10333 -q .1.3.6.1.2.1.2.2.1.1 localhost
 * 
 * </pre>
 */
public class snmpinform extends snmp
{
    long    _sysUpTime   = 0;
    String  _snmpTrapOID = ".1.3";
    SnmpVarBindList _vblist = new SnmpVarBindList();

    public static void main(String[] args)
    {
        snmpinform s = new snmpinform();
        s.parseOptions(args, "snmpinform");
        // s.printOptions();
        s.inform();
    }
    
    /**
     * use SnmpSession.snmpGetNextRequest
     */
    private void inform()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }
            SnmpVarBind [] vbs = null;
            if(_vblist.size() > 0)
            {
                vbs = _vblist.toArray();
            }
            SnmpPdu retPdu = session.snmpInformRequest(_sysUpTime, new SnmpOID(_snmpTrapOID), vbs);
            session.close();
        }
        catch(TimeoutException timeout)
        {
            System.out.println( "time out");
        }
        catch(IOException e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // Parsing command line options
    // ----------------------------------------------------------------------
    
    /**
     * Prints usage lines without explanation lines
     */
    protected void usage(String programName, boolean allowMultipleOIDs)
    {
        System.out.println( "Usage: java " + programName + " [options...] <hostname>\n");
    }

    protected void moreOptions()
    {
        System.out.println( "-s <s>\tsysUpTime or timestamp");
        System.out.println( "-q <o>\tsnmpTrapOID");
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

        int start = 1;
        while(start < as.length)
        {
            String oid = as[start];
            String type = as[start + 1];
            String value = as[start + 2];
            SnmpDataType t = translate(type, value);
            SnmpVarBind vb = new SnmpVarBind(oid, t);
            _vblist.add(vb);
            start += 3;
        }
    }

    protected void printExample(String programName)
    {
        System.out.println( "java " + programName + 
                " -s 10333 -q .1.3.6.1.2.1.2.2.1.1 localhost" ); 
        System.out.println( "java " + programName + 
                " -s 10333 -q .1.3.6.1.2.1.2.2.1.7 " + "localhost 1.3.6.1.2.1.2.2.1.7.3 i 1" );
        System.out.println( "java " + programName + 
                " -v 3 -u newUser -A abc12345 -X abc12345 -s 10333 -q .1.3.6.1.2.1.2.2.1.1 " +
                "localhost" );
    }

    protected void parseOptions(String[] args, String program)
    {
        super.parseOptions(args, program);
        _sysUpTime   = Integer.parseInt(_parseArgs.getOptionValue('s', "0"));
        _port = Integer.parseInt(_parseArgs.getOptionValue('p', "162"));
    }

    protected void printMoreOptions()
    {
        System.out.println( "sysUpTime =\t\t" + _sysUpTime);
        System.out.println( "snmpTrapOID =\t\t" + _snmpTrapOID);
    }

    /**
     * Creates a new instance of ParseArguments
     */
    protected ParseArguments newParseArgumentsInstance(String[] args)
    {
        return new ParseArguments(args, "?ho", "utvaAXxcpmsq");
    }
    
}// end of class snmpinform 

