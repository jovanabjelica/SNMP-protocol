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


import com.ireasoning.protocol.*;
import com.ireasoning.protocol.snmp.*;
import com.ireasoning.util.ParseArguments;

/**
 * This class demonstrates an implementations of SNMP SET operation. 
 * <pre>
 * Example:
 * java snmpset localhost .1.3.6.1.2.1.1.4.0 s test
 * java -c setcommunity snmpset localhost .1.3.6.1.2.1.1.4.0 s test
 * java snmpset localhost .1.3.6.1.2.1.1.4.0 s test .1.3.2.12.1.3.0 i 345
 * 
 * </pre>
 */
public class snmpset extends snmp
{
    SnmpVarBindList _vblist = new SnmpVarBindList();
    
    public static void main(String[] args)
    {
        snmpset s = new snmpset();
        s.parseOptions(args, "snmpset");
        // s.printOptions();

        s.snmpSet();
    }
    
    private void snmpSet()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }
            
            SnmpPdu pdu = new SnmpPdu(SnmpConst.SET);
            pdu.addVarBinds(_vblist);
            
            SnmpPdu retPdu = session.snmpSetRequest(pdu);

            print(retPdu);
            session.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
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
            SnmpOID o = MibUtil.lookupOID(oid);          
            String type = as[start + 1];
            String value = as[start + 2];
            SnmpDataType t = translate(type, value);
            SnmpVarBind vb = new SnmpVarBind(o, t);
            _vblist.add(vb);
            start += 3;
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
        System.out.println( "Usage: java " + programName +
                " [options...] <hostname> [<OID> <type> <value> ...]\n");
        System.out.println( "<OID>\tobject identifier");
        System.out.println( "<type>\tdata type of the value, one of i, u, t, a, o, s, c, g." +
                " i: INTEGER, u: unsigned INTEGER, t: TIMETICKS, " + 
                "a: IPADDRESS, o: OID, s: STRING, c: counter, g: gauge, , x: hex data (in \"0x1B 0xAC ...\" format)");
        System.out.println( "<value>\tvalue of this object identifier");
    }

    /**
     * Prints out the example usage
     */
    protected void printExample(String programName)
    {
        System.out.println( "java " + programName + " localhost .1.3.6.1.2.1.1.4.0 s test");
        System.out.println( "java " + programName + " -c setcommunity localhost .1.3.6.1.2.1.1.4.0 s test");
        System.out.println( "java " + programName + " localhost .1.3.6.1.2.1.1.4.0 s test " +
                ".1.3.2.12.1.3.0 i 345");
    }
}// end of class snmpset 
