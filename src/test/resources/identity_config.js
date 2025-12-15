$$.spaces=3
//$$.replacer=['lastName','firstName'] //broken
$$.replacer=(k,v)=>['address','phoneNumber'].includes(k)?null:['age'].includes(k)?undefined:v;
$ = _