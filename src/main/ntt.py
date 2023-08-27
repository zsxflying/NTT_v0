import random
from sympy import ntt

# 快速幂
def big_num_mod_pow(a, b, mod):
    if b == 0:
        return 1
    res = 1
    for i in range(b):
        res *= a
        res %= mod
    return res

# 旋转因子
def vandermonde(polyLen, root, mod):
    matrix = [[] for i in range(polyLen)]
    for i in range(polyLen):
        root_i = big_num_mod_pow(root, i, mod)
        for j in range(polyLen):
            root_i_j = big_num_mod_pow(root_i, j, mod)
            matrix[i].append(root_i_j)
    return matrix


mod = 65537
g = 3


root_16 = big_num_mod_pow(g, (mod-1)//16, mod)

# inputArray_16 = [random.randint(0, mod) for i in range(16)]
# res_16 = ntt(inputArray_16, mod)

root_4 = big_num_mod_pow(g,(mod-1)//4,mod)
inputArray_4 = [56199, 14191, 12134,12035]
res_4 = ntt(inputArray_4, mod)


# print("vandermonde_16: ", vandermonde(16, root_16, mod))
# print("inputArray_16: ", inputArray_16)
# print("res_16: ", res_16)

print("vandermonde_4: ", vandermonde(4, root_4, mod))
print("inputArray_4: ", inputArray_4)
print("res_4: ", res_4)