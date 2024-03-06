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


import com.ireasoning.protocol.Session;
import com.ireasoning.protocol.snmp.*;
import com.ireasoning.util.ParseArguments;
import java.io.IOException;

/**
 * This example demonstrates how to send SNMPv1, v2c, and v3 traps
 * <pre>
 * Example:
 * java snmptrap -s 10333 -n .1.3.6.1.2.1.2.2.1.1 -g 2 localhost
 * java snmptrap -s 10333 -n .1.3.6.1.2.1.2.2.1.1 -g 6 -i 20 localhost
 * java snmptrap -t 2 -s 10333 -q .1.3.6.1.2.1.2.2.1.1 localhost
 * java snmptrap -t 2 -s 10333 -q .1.3.6.1.2.1.2.2.1.7 localhost 1.3.6.1.2.1.2.2.1.7.3 i 2 //with variable ifAdminStatus.3
 * java snmptrap -v 3 -u newUser -A abc12345 -X abc12345 -e 12345  -s 10333 -q .1.3.6.1.2.1.2.2.1.1 localhost
 * 
 * </pre>
 */
public class snmptrap extends snmp
{
    byte[] _engineID = "testengine".getBytes();
    long    _sysUpTime   = 0;
    String  _snmpTrapOID = ".1.3";
    String  _enterprise  = ".1.3";
    String  _ipAddress   = "";
    int     _generic     = 0;
    int     _specific    = 0;
    int     _trapVersion = 1;

    SnmpVarBindList _vblist = new SnmpVarBindList();

    public static void main(String[] args)
    {
        snmptrap s = new snmptrap();
        s.parseOptions(args, "snmptrap");
        s.sendTrap();
    }

    void sendTrap()
    {
        try
        {
            if(_isSnmpV3)
            {
                _trapVersion = 2;
            }

            if(_trapVersion == 1)
            {//send SNMPV1 trap
                SnmpV1Trap trap = new SnmpV1Trap(_enterprise);
                trap.setTimestamp(_sysUpTime);
                trap.setGeneric(_generic);
                trap.setSpecific(_specific);
                trap.addVarBinds(_vblist);
                if(_ipAddress != null && _ipAddress.length() > 0)
                {
                    trap.setIpAddress(_ipAddress);
                }
                SnmpTrapSender.sendTrap(trap, _host, _port, _community, createPacketSender());
            }
            else
            {//Send SNMPV2 or V3 traps
                if(_isSnmpV3)
                {//sets SNMPV3 parameters
                    SnmpTrapSender.addV3Params(_user, _authProtocol, _authPassword, _privProtocol,
                            _privPassword, _engineID, _host, _port);
                }
                SnmpTrap trap = new SnmpTrap(_sysUpTime, new SnmpOID(_snmpTrapOID));
                trap.addVarBinds(_vblist);
                SnmpTrapSender.sendTrap(_host, _port, trap, true, _community, createPacketSender());
            }
        }
        catch(IOException e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
        catch(SnmpEncodingException ee)
        {
            System.out.println( ee );
            ee.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // Parsing command line options
    // ----------------------------------------------------------------------
    
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

    /**
     * Prints usage lines without explanation lines
     */
    protected void usage(String programName, boolean allowMultipleOIDs)
    {
        System.out.println( "Usage: java " + programName +
                " [options...] <hostname> [<OID> <type> <value> ...]\n");
        System.out.println( "<OID>\tobject identifier");
        System.out.println( "<type>\tdata type of the value, one of i, u, t, a, o, s." +
                " i: INTEGER, u: unsigned INTEGER, t: TIMETICKS, " + 
                "a: IPADDRESS, o: OID, s: STRING");
        System.out.println( "<value>\tvalue of this object identifier");
    }

    protected void moreOptions()
    {
        System.out.println( "-e <e>\ttrap sender's engine ID");
        System.out.println( "-t <1|2>\ttrap version, possible values are 1 or 2");
        System.out.println( "-s <s>\tsysUpTime or timestamp");
        System.out.println( "-q <o>\tsnmpTrapOID");
        System.out.println( "-n <n>\tenterprise OID");
        System.out.println( "-g <g>\tSNMPv1 generic code");
        System.out.println( "-i <i>\tSNMPv1 enterprise specific code");
        System.out.println( "-f <f>\tSNMPv1 trap originator's IP address");
    }
    
    protected void printExample(String programName)
    {
        System.out.println( "java " + programName + 
                " -s 10333 -n .1.3.6.1.2.1.2.2.1.1 -g 2 localhost" ); 
        System.out.println( "java " + programName + 
                " -s 10333 -n .1.3.6.1.2.1.2.2.1.1 -g 6 -i 20 localhost" ); 
        System.out.println( "java " + programName + 
                " -t 2 -s 10333 -q .1.3.6.1.2.1.2.2.1.1 " + "localhost" );
        System.out.println( "java " + programName + 
                " -t 2 -s 10333 -q .1.3.6.1.2.1.2.2.1.7 " + "localhost 1.3.6.1.2.1.2.2.1.7.3 i 1" );
        System.out.println( "java " + programName + 
                " -v 3 -u newUser -A abc12345 -X abc12345 -s 10333 -q .1.3.6.1.2.1.2.2.1.1 " +
                "localhost" );
        System.out.println( "java " + programName + 
                " -v 3 -u newUser -A abc12345 -X abc12345 -e 12345  -s 10333 -q .1.3.6.1.2.1.2.2.1.1 " +
                "localhost" );
    }

    protected void parseOptions(String[] args, String program)
    {
        super.parseOptions(args, program);
        String engineID = _parseArgs.getOptionValue('e');
        if(engineID != null)
        {
            _engineID = snmp.getHexString(engineID);
        }
        _trapVersion = Integer.parseInt(_parseArgs.getOptionValue('t', "1"));
        _snmpTrapOID = _parseArgs.getOptionValue('q');
        _enterprise  = _parseArgs.getOptionValue('n');
        _generic = Integer.parseInt(_parseArgs.getOptionValue('g', "0"));
        _specific = Integer.parseInt(_parseArgs.getOptionValue('i', "0"));
        _sysUpTime   = Long.parseLong(_parseArgs.getOptionValue('s', "0"));
        _port = Integer.parseInt(_parseArgs.getOptionValue('p', "162"));
        _ipAddress = _parseArgs.getOptionValue('f');
    }

    protected void printMoreOptions()
    {
        System.out.println( "engine ID =\t\t" + _parseArgs.getOptionValue('e'));
        System.out.println( "trap version =\t\t" + _trapVersion);
        System.out.println( "sysUpTime =\t\t" + _sysUpTime);
        System.out.println( "snmpTrapOID =\t\t" + _snmpTrapOID);
        System.out.println( "enterpriseOID = \t" + _enterprise);
        System.out.println( "SNMPv1 generic =\t\t" + _generic);
        System.out.println( "SNMPv1 specific =\t\t" + _specific);
    }

    /**
     * Creates a new instance of ParseArguments
     */
    protected ParseArguments newParseArgumentsInstance(String[] args)
    {
        return new ParseArguments(args, "?ho", "uvaAXxcpmetsqngifk");
    }

    private PacketSender createPacketSender()
    {
        if(_transportLayer == Session.UDP)
        {
            return new UdpPacketSender();
        }
        else
        {
            return new TcpPacketSender();
        }
    }
}// end of class 

