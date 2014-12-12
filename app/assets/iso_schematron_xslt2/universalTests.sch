<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" 
   queryBinding="xslt" >
  <sch:title>Universal Tests</sch:title>
  <sch:p>This schema gives the most basic tests for checking an ISO Schematron implementation.
  It does not test the XPath implementation in any significant way, just the basics of assertions,
  rules, patterns, and phases. </sch:p>
  
  <sch:phase id="positive">
    <sch:active pattern="p1"/>
  </sch:phase>
  
  <sch:phase id="negative">
    <sch:active pattern="p2"/>
  </sch:phase>
  
  <sch:pattern  id="p1">
     <sch:title>Always True</sch:title>
     
     <sch:rule context="/">
     	<sch:assert test="true()" >U1: This assertion should never fail.</sch:assert>
     	<sch:report test="false()" >U2: This report should never succeeed.</sch:report>
     </sch:rule>
 
     <sch:rule context="/*">
     	<sch:assert test="true()" >U3: This assertion should never fail.</sch:assert>
     	<sch:report test="false()" >U4: This report should never succeeed.</sch:report>
     </sch:rule> 
     
     <!-- Test rule fallthrough -->
     <sch:rule context="/*">
     	<sch:assert test="false()" >U5: This assertion should never succeed because the rule should never fire.</sch:assert>
     	<sch:report test="true()" >U6: This report should never succeeed because the rule should never fire.</sch:report>
     </sch:rule> 
 
  </sch:pattern>
  
  <sch:pattern  id="p2">
     <sch:title>Always False</sch:title>
     
     <sch:rule context="/">
     	<sch:assert test="false()" >U7: This assertion should always fail.</sch:assert>
     	<sch:report test="true()" >U8: This report should always succeeed.</sch:report>
     </sch:rule>
     
     <sch:rule context="/*">
     	<sch:assert test="false()" >U9: This assertion should always fail.</sch:assert>
     	<sch:report test="true()" >U10: This report should always succeeed.</sch:report>
     </sch:rule>
     
     <!-- Test rule fallthrough -->
     <sch:rule context="/*">
     	<sch:assert test="false()" >U11: This assertion should never succeed because the rule should never fire.</sch:assert>
     	<sch:report test="true()" >U12: This report should never succeeed because the rule should never fire.</sch:report>
     </sch:rule> 
  </sch:pattern>
  
</sch:schema>