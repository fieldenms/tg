var found = 0;
var n = 1;
var THRESHOLD = 1000000;

while (n < THRESHOLD) {
    n += 1;
   //  for (var i = 2; i <= Math.sqrt(n); i++) {
   //      if (!(n % i == 0)) {
			// total++;
   //          postMessage(found);
   //      }
   //      else {
   //          found++;
   //      }
   //  }
    var isPrime = true;

    for (var i = 2; i <= Math.sqrt(n); i++) {
        if (n % i === 0) {
            isPrime = false;
            break;
        }
    }

    if (isPrime) {
        postMessage(n);    
    }

}