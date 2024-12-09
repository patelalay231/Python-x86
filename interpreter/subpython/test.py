
def factorial(n):
    if n == 0:
        return 1
    else:
        return factorial(n-1) * n

def fabonacci(n):
    if n == 0:
        return 0
    elif n == 1:
        return 1
    else:
        return fabonacci(n-1) + fabonacci(n-2)
if !1:
    print("hello")

print(factorial(5))
for i in range(10):
    print(fabonacci(i))

def foo(x, y):
    if x and y:
        print("xy")
    elif x:
        print("x")
    else:
        print("z")

def bar(x, y):
    if x:
        if y:
            print("xy")
    elif x:
        print("x")
    else:
        print("z")

def foo(x, r):
    if x != 0 and r > 1/100:
        print("Y")

x = 2
foo(x, 1/x)

for i in range(5):
    print(i)
    i = i + 1


count = 3

if count % 2:
    print("odd")
else:
    print("even")


str = "hello"

str[1]  = "1"
print(str)
print(str + " world")
