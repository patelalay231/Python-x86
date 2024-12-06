# factorial

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