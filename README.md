# ldap2zammad

Create, update and delete zammad user entries from a selected LDAP directory server.

## LDAP to Zammad attribute mapping

The mapping will be done within a JavaScript function.
This `function` will be executed for every entry in the LDAP resultset.
 
```javascript
(
  function ldap2zammad( mode, zammadUser, ldapEntry, config )
  {
    // mode : "test", "create, "update"
    
    zammadUser.setFirstname(ldapEntry.getAttributeValue("givenname"));
    zammadUser.setLastname(ldapEntry.getAttributeValue("sn"));
    zammadUser.setEmail(ldapEntry.getAttributeValue("mail"));
    zammadUser.setPhone(ldapEntry.getAttributeValue("telephoneNumber"));
    zammadUser.setFax(ldapEntry.getAttributeValue("facsimileTelephoneNumber"));
    zammadUser.setWeb("https://www.myorg.de");
    zammadUser.setOrganization("MyOrg");
    zammadUser.setVerified(true);

    if ( "create" === mode || "test" === mode )
    {
      if ( ldapEntry.getAttributeValue("institute") === "CC" )
      {
        zammadUser.setDepartment( "CC" );
        zammadUser.setRoles( [ "Agent", "Customer" ] );
      }
      else
      {
        zammadUser.setRoles( [ "Customer" ] ); // array of String
      }
    }
  }
);
```

