x = 10
y = 20
z = "hello"
result = ""
result_nested = ""
final_result = ""
# Complex if-elif-else chain
if x > y:
    result = "x is greater than y"
elif x == y:
    result = "x is equal to y"
elif x < y and z == "hello":
    result = "x is less than y and z says hello"
elif x < y and z == "goodbye":
    result = "x is less than y and z says goodbye"
else:
    result = "none of the conditions were met"

# Additional nested if-else for complexity
if x < y:
    if z == "hello":
        result_nested = "x is less and z greets"
    elif z == "world":
        result_nested = "x is less and z says world"
    else:
        result_nested = "x is less and z says something else"
else:
    result_nested = "x is not less than y"

# Another level of nesting with else-if chain
if y == 20:
    if x == 10:
        if z == "hello":
            final_result = "Perfect match"
        elif z == "world":
            final_result = "Close match"
        else:
            final_result = "Mismatch"
    elif x < 10:
        final_result = "x is too small"
    else:
        final_result = "x is too large"
else:
    final_result = "y is not 20"

print(result)
print(result_nested)
print(final_result)

