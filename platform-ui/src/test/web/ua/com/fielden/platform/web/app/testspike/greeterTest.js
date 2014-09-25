define(['app/testspike/greeter', 'customLib', 'customNonAmdLib', 'angular', 'angular.mocks'], function(greeter, customLib, CustomNonAmdLib, angular, angularMocks) {
    describe('Greeter module', function () {
        
        it(" must return 'Hello, World' for 'World' parameter.", function() {
            expect(greeter('World')).toBe('Hello, World');
        });

        it(" must return 'Hello, Jhou' for 'Jhou' parameter.", function() {
            expect(greeter('Jhou')).toBe('Hello, Jhou');
        });

        it(" should happen, that trim() function exist.", function() {
        	// alert(typeof String.prototype.trim);
        	// console.log(typeof String.prototype.trim);

            expect(' Jhou '.trim()).toBe('Jhou');
        });

        it(" invokes angular simple function.", function() {
            expect(angular.module("example", [])).not.toBeNull();
        });

        it(" invokes angular-mocks simple function.", function() {
            angular.module("example", []);

            // module("example");


            expect(typeof module).toBe("function");
            expect(module).not.toBe(angular.module);
            

            //expect(module("example")).not.toThrow();
        });
    });
});