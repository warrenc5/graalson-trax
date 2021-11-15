# graalson-trax
Javax.TRAX EE bindings for Graalson graaljs

https://docs.oracle.com/javase/8/docs/api/javax/xml/transform/TransformerFactory.html

Common pattern to transform JSON with Javascript directly on graal vm.


Load from json resource

        {
             "firstName": "John", "lastName": "Smith", "age": 25,
             "address" : {
                 "streetAddress": "21 2nd Street",
                 "city": "New York",
                 "state": "NY",
                 "postalCode": "10021"
             },
             "phoneNumber": [
                 { "type": "home", "number": "212 555-1234" },
                 { "type": "fax", "number": "646 555-4567" }
             ]
        }
  
Load transformer 

        delete _.address['postalCode']

        let geo = lookupGeo(_.address),
        $ =  {
            name: _.firstName + ' ' + _.lastName,
            'year-of-birth': 2021-_.age,
            address : {
                ... _.address,
                geo
            },
            phoneNumber: _.phoneNumber.filter(p=>p.type == 'home')[0].number
        }

Call transform 

        System.setProperty("javax.xml.transform.TransformerFactory", "au.com.devnull.graalson.trax.GraalsonTransformerFactory");

        Map<String, Object> config = new HashMap<>();
        config.put("spaces", Integer.valueOf(4));
        JsonWriterFactory wfactory = Json.createWriterFactory(config);
        JsonWriter jwriter = wfactory.createWriter(new PrintWriter(System.out));
        JsonReader jreader = Json.createReader(ClassLoader.getSystemClassLoader().getResourceAsStream("default.json"));

        GraalsonSource template = new GraalsonSource("template1.js");
        GraalsonSource source = new GraalsonSource(jreader);
        GraalsonResult result = new GraalsonResult(jwriter);

        TransformerFactory.newInstance().newTemplates(template).newTransformer().transform(source, result);

Output 


        {
            "address": {
                "streetAddress": "21 2nd Street",
                "city": "New York",
                "state": "NY",
                "country": "Javaland"
            },
            "phoneNumber": "212 555-1234",
            "name": "John Smith",
            "year-of-birth": 1996
        }

Add dependency
            
        <dependency>
            <groupId>biz.mofokom</groupId>
            <artifactId>graalson-trax</artifactId>
            <version>1.0.0</version>
        </dependency>

