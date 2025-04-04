import inspect
import types
from typing import Union

def _normalize_type(annotation):
    """
    If the annotation is None convert it to type(None); otherwise, return the annotation unchanged.
    """
    if annotation is None:
        return type(None)
    return annotation

def _check_type(value, annotation):
    """
    Returns True if 'value' conforms to the type hint 'annotation'.

    - If annotation is inspect._empty, no check is performed (returns True).
    - If annotation is a UnionType (Python 3.10+, e.g. int|str), checks whether
      value is an instance of at least one type in annotation.__args__.
    - Otherwise, uses isinstance(value, annotation).
    """
    if annotation == inspect._empty:
        return True

    annotation = _normalize_type(annotation)

    if type(annotation) is types.UnionType:
        return any(isinstance(value, t) for t in annotation.__args__)
    #otherwise annotation is a single type
    return isinstance(value, annotation)

def print_types(func):
    """
    This decorator prints each parameter's type annotation and its actual type
    before the function is called, then calls the function and finally prints
    the return type annotation along with the actual type of the result.
    """
    def wrapper(*args, **kwargs):
        sig = inspect.signature(func)
        params = list(sig.parameters.values())

        # Print parameter information
        for i, arg in enumerate(args):
            if i < len(params):
                ann = params[i].annotation
                pname = params[i].name
            else:
                ann = inspect._empty
                pname = f'param_{i}'
            print(f"Formal par '{pname}':{ann}; actual par '{arg}':{type(arg)}")

        # Call the function
        result = func(*args, **kwargs)

        # Print return value information
        ret_annot = sig.return_annotation
        print(f"Result type {ret_annot}; actual result '{result}':{type(result)}")

        return result

    return wrapper

def type_check(func):
    """
    This decorator prints messages for any type mismatches found in the parameters
    or the return value (if annotated), using the following format:
    Parameter 'i' has value 'val', not of type 'ann'
    -and
    Result is 'val', not of type 'ann'
    """
    def wrapper(*args, **kwargs):
        sig = inspect.signature(func)
        params = list(sig.parameters.values())

        # Check parameters for type mismatches
        for i, arg in enumerate(args):
            if i < len(params):
                ann = params[i].annotation
                if ann != inspect._empty and not _check_type(arg, ann):
                    print(f"Parameter '{i}' has value '{arg}', not of type '{ann}'")

        # Execute the function
        result = func(*args, **kwargs)

        # Check the return value's type
        ret_ann = sig.return_annotation
        if ret_ann != inspect._empty and not _check_type(result, ret_ann):
            print(f"Result is '{result}', not of type '{ret_ann}'")

        return result

    return wrapper

def bb_type_check(max_block):
    """
    If there are type mismatches in the parameters and at least one block is available,
    prints the mismatches, decrements the block count, and blocks the function call (returning None)
    with the message: "Function blocked. Remaining blocks: X".
    If no blocks remain or there are no mismatches, the function is executed normally,
    and any mismatches are printed accordingly.
    """
    def decorator(func):
        blocks_remaining = max_block  
        
        def wrapper(*args, **kwargs):
            nonlocal blocks_remaining  # to allow modification 
            sig = inspect.signature(func)
            params = list(sig.parameters.values())

            # Check for parameter type mismatches
            mismatches = []
            for i, arg in enumerate(args):
                if i < len(params):
                    ann = params[i].annotation
                    if ann != inspect._empty and not _check_type(arg, ann):
                        mismatches.append((i, arg, ann))

            # If mismatches exist and blocks are available, block the function call
            if mismatches and blocks_remaining > 0:
                for idx, val, ann in mismatches:
                    print(f"Parameter '{idx}' has value '{val}', not of type '{ann}'")
                blocks_remaining -= 1
                print(f"Function blocked. Remaining blocks: {blocks_remaining}")
                return None

            # Otherwise, print any mismatches and execute the function
            for idx, val, ann in mismatches:
                print(f"Parameter '{idx}' has value '{val}', not of type '{ann}'")

            result = func(*args, **kwargs)

            # Check for mismatches in the return type
            ret_ann = sig.return_annotation
            if ret_ann != inspect._empty and not _check_type(result, ret_ann):
                print(f"Result is '{result}', not of type '{ret_ann}'")

            return result

        return wrapper

    return decorator
