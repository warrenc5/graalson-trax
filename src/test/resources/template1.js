if(!_)
    throw '_ undefined';

function lookupCountry (cityName) {
    return 'Javaland'
}

console.log(_)
console.log('source', JSON.stringify(_))
console.log('')
delete _.address['postalCode']

var result =  {
    'name': _.firstName + ' ' + _.lastName,
    'year-of-birth': 2021-_.age,
    'address' : {
        ... _.address,
        'country': lookupCountry(_.city)
    },
    'phoneNumber': _.phoneNumber.filter(p=>p.type == 'home')[0].number
}


console.log('result', JSON.stringify(result))
console.log('')