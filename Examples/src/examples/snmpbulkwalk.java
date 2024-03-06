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

/**
 * This class demonstrates an implementation of SNMP WALK by using
 * more efficient SNMP GET_BULK operation.
 * Note: Agent has to be able to support SNMPV2.
 * <pre>
 * Example:
 * java snmpbulkwalk localhost .1.3.2
 * java snmpbulkwalk localhost
 * java snmpbulkwalk localhost -v 3 -u newUser -A abc12345 -X abc12345 .1.3.2
 * </pre>
 */
public class snmpbulkwalk extends snmp
    implements Listener
{
    
    public static void main(String[] args)
    {
        snmpbulkwalk s = new snmpbulkwalk();
        s.parseOptions(args, "snmpbulkwalk", false, false);

        s.walk();
    }
    
    private void walk()
    {
        try
        {
            SnmpSession session = new SnmpSession(_host, _port, _community,
                    _community, _version, _transportLayer);
            if(_isSnmpV3)
            {
                session.setV3Params(_user, _authProtocol, _authPassword, _privProtocol, _privPassword, _context, null);
            }
            session.snmpBulkWalk(_oids[0], this);
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    
    public void handleMsg(Object session, Msg msg)
    {
        SnmpPdu pdu = (SnmpPdu) msg;
        if(!pdu.hasMore() && pdu.getVarBindCount() == 0)
        {
            System.out.println( "<End of MIB View Reached>");
            ((SnmpSession)session).close();
            return;
        }

        print(pdu);

        if(!pdu.hasMore())
        {
            System.out.println( "<End of MIB View Reached>");
            ((SnmpSession)session).close();
            return;
        }
    }
    
}// end of class snmpbulkwalk 
