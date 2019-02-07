#!/usr/bin/env python3

import os
import subprocess
import difflib
import shutil


JAVA_RUN = 'java -classpath out/production/SWEN30006_2018S2_PartB automail.Simulation'
RESOURCE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), 'resources'))

def run_program():
    project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir))
    os.chdir(project_root)
    err = None
    try:
        result = subprocess.run(JAVA_RUN.split(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except subprocess.CalledProcessError as e:
        err = e
        return (result.stderr, err)
    stdout = result.stdout.decode('utf-8') if result.stdout is not None else ''
    stderr = result.stderr.decode('utf-8') if result.stderr is not None else ''
    return (stdout, stderr, err)


def swap_prop_file(new_prop):
    old_prop= os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir,
                                           'automail.properties'))
    shutil.move(old_prop, old_prop + '.bak')
    shutil.copy2(new_prop, old_prop)


def cleanup_prop_file():
   prop = os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir,
                                       'automail.properties'))
   shutil.move(prop + '.bak', prop)


def test_behviour(prop_file='standard_weak_behaviour.properties'):
    file_name = os.path.join(RESOURCE_DIR, 'expected_output.txt')
    expected = ''
    swap_prop_file(os.path.join(RESOURCE_DIR, prop_file))
    with open(file_name) as fh:
        expected = fh.read()
    stdout, stderr, err = run_program()
    cleanup_prop_file()

    # Filter out lines which are common in both sequences
    # str.splitlines() with no arguments ignores the line break
    # characters to help with making the diff cross platform
    diff = [diff for diff in difflib.ndiff(expected.splitlines(), stdout.splitlines())
            if diff[0] != ' ']
    if err:
        print('Failed')
        print(stderr)
        print(stdout)
        return False
    elif diff:
        print('Failed')
        print('\n'.join(diff))
        return False
    else:
        print('Passed')
        return True


def test_fragile(prop_file='breakage_conditions.properties'):
    swap_prop_file(os.path.join(RESOURCE_DIR, prop_file))
    stdout, stderr, err = run_program()
    cleanup_prop_file()
    if 'Fragile item broken!!' in stderr:
        print('Passed')
        return True
    print('Failed')
    print(stderr)
    print(stdout)
    return False

def test_properties(prop_file='properties.properties'):
    swap_prop_file(os.path.join(RESOURCE_DIR, prop_file))
    stdout, stderr, err = run_program()
    cleanup_prop_file()
    for prop in ['Seed', 'Floors', 'Fragile', 'Mail_to_Create', 'Last_Delivery_Time']:
        if prop not in stdout:
            print('Failed')
            print(stderr)
            print(stdout)
            return False
    print('Passed')
    return True


def main():
    print('Acceptance tests for orginal behaviour of SWEN30006 Part B')

    print('Testing for same output with Standard and Weak robots')
    behaviour_ok = test_behviour()

    print('Testing for breakage with fragile items')
    fragile_ok = test_fragile()

    print('Testing for support of orginal properties')
    properties_ok = test_properties()

    print('\n\n\n-------------------------------------------------------------\n\n')
    print('Overview')

    if behaviour_ok:
        print('Behaviour test: PASSED')
    else:
        print('Behaviour test: FAILED')

    if fragile_ok:
        print('Fragile test: PASSED')
    else:
        print('Fragile test: FAILED')

    if properties_ok:
        print('Properties test: PASSED')
    else:
        print('Properties test: FAILED')

    exit()


if __name__ == '__main__':
    main()
