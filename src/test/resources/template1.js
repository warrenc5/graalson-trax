if(!_)
    throw '_ undefined';

function lookupGeo (address) {
    var geo = { x: 100.0, y: 200 }
    return geo;
}

//console.log(_)
//console.log('source', JSON.stringify(_))
//console.log('')
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
console.log('result', JSON.stringify($))